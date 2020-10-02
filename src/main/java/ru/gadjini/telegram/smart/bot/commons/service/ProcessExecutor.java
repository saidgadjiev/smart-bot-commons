package ru.gadjini.telegram.smart.bot.commons.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.exception.ProcessException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Service
public class ProcessExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutor.class);

    @Value("${process.logging.dir}")
    private String processLoggingDir;

    @PostConstruct
    public void init() {
        LOGGER.debug("Process logging dir({})", processLoggingDir);
        try {
            File loggingDirFile = new File(processLoggingDir);
            if (!loggingDirFile.exists()) {
                Files.createDirectory(loggingDirFile.toPath());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public String executeWithResult(String[] command) {
        return execute(command, ProcessBuilder.Redirect.PIPE, null, Collections.emptySet());
    }

    public void executeWithFile(String[] command, String outputFile) {
        execute(command, null, outputFile, Collections.emptySet());
    }

    public void execute(String[] command, Collection<Integer> successCodes) {
        execute(command, ProcessBuilder.Redirect.DISCARD, null, successCodes);
    }

    public void execute(String[] command) {
        execute(command, ProcessBuilder.Redirect.DISCARD, null, Collections.emptySet());
    }

    private String execute(String[] command, ProcessBuilder.Redirect redirectOutput, String outputRedirectFile, Collection<Integer> successCodes) {
        File errorFile = getErrorLogFile();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            if (redirectOutput != null) {
                processBuilder.redirectOutput(redirectOutput);
            } else if (StringUtils.isNotBlank(outputRedirectFile)) {
                processBuilder.redirectOutput(new File(outputRedirectFile));
            }
            if (errorFile != null) {
                processBuilder.redirectError(errorFile);
            }
            Process process = processBuilder.start();
            try {
                Set<Integer> codes = new HashSet<>(successCodes);
                codes.add(0);
                int exitValue = process.waitFor();
                if (!codes.contains(exitValue)) {
                    LOGGER.error("Error({}, {}, {})", process.exitValue(), Arrays.toString(command), errorFile != null ? errorFile.getName() : "404");
                    throw new ProcessException("Error " + process.exitValue() + "\nCommand " + Arrays.toString(command) + "\nLogs: " + (errorFile != null ? errorFile.getName() : "404"));
                } else if (exitValue != 0) {
                    LOGGER.error("Completed with strange exit code({}, {}, {})", exitValue, Arrays.toString(command), errorFile != null ? errorFile.getName() : "404");
                }

                String result = null;
                if (redirectOutput == ProcessBuilder.Redirect.PIPE) {
                    result = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
                }

                FileUtils.deleteQuietly(errorFile);
                return result;
            } finally {
                process.destroy();
            }
        } catch (Exception ex) {
            if (ex instanceof ProcessException) {
                throw (ProcessException) ex;
            } else {
                throw new ProcessException(ex);
            }
        }
    }

    private File getErrorLogFile() {
        try {
            return File.createTempFile("log", ".txt", new File(processLoggingDir));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);

            return null;
        }
    }
}
