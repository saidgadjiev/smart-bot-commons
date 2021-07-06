package ru.gadjini.telegram.smart.bot.commons.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.exception.ProcessException;
import ru.gadjini.telegram.smart.bot.commons.exception.ProcessTimedOutException;
import ru.gadjini.telegram.smart.bot.commons.utils.SmartFileUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ProcessExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutor.class);

    @Value("${process.logging.dir:#{null}}")
    private String processLoggingDir;

    @PostConstruct
    public void init() {
        if (StringUtils.isNotBlank(processLoggingDir)) {
            LOGGER.debug("Process logging dir({})", processLoggingDir);
            File loggingDirFile = new File(processLoggingDir);
            SmartFileUtils.mkdirs(loggingDirFile);
        }
    }

    public String executeWithResult(String[] command) throws InterruptedException {
        File out = createTempFile();

        try {
            return execute(command, null, out.getAbsolutePath(), Collections.emptySet(), true);
        } finally {
            FileUtils.deleteQuietly(out);
        }
    }

    public void executeWithFile(String[] command, String outputFile) throws InterruptedException {
        execute(command, null, outputFile, Collections.emptySet(), false);
    }

    public void execute(String[] command, Collection<Integer> successCodes) throws InterruptedException {
        execute(command, null, null, successCodes, false);
    }

    public void execute(String[] command) throws InterruptedException {
        execute(command, null, null, Collections.emptySet(), false);
    }

    public void execute(String[] command, int waitForSeconds) throws InterruptedException {
        execute(command, waitForSeconds, null, Collections.emptySet(), false);
    }

    public String tryExecute(String[] command, int waitForInSeconds) throws InterruptedException {
        File errorFile = getErrorLogFile();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(errorFile));
            processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(errorFile));
            Process process = processBuilder.start();
            try {
                process.waitFor(waitForInSeconds, TimeUnit.SECONDS);

                return FileUtils.readFileToString(errorFile, StandardCharsets.UTF_8);
            } finally {
                process.destroy();
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception ex) {
            if (ex instanceof ProcessException) {
                throw (ProcessException) ex;
            } else {
                throw new ProcessException(ex);
            }
        } finally {
            FileUtils.deleteQuietly(errorFile);
        }
    }

    private String execute(String[] command, Integer waitForSeconds,
                           String outputRedirectFile, Collection<Integer> successCodes, boolean returnResult) throws InterruptedException {
        File errorFile = getErrorLogFile();
        try {
            FileUtils.writeStringToFile(errorFile, String.join(" ", command) + "\n", StandardCharsets.UTF_8);
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            if (StringUtils.isNotBlank(outputRedirectFile)) {
                processBuilder.redirectOutput(new File(outputRedirectFile));
            } else {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            }
            processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(errorFile));
            Process process = processBuilder.start();
            try {
                Set<Integer> codes = new HashSet<>(successCodes);
                codes.add(0);
                int exitValue;
                if (waitForSeconds == null) {
                    exitValue = process.waitFor();
                } else {
                    exitValue = process.waitFor(waitForSeconds, TimeUnit.SECONDS) ? 0 : 1;
                    if (exitValue == 1) {
                        throw new ProcessTimedOutException();
                    }
                }
                if (!codes.contains(exitValue)) {
                    LOGGER.error("Error({}, {}, {})", process.exitValue(), Arrays.toString(command), errorFile.getName());
                    throw new ProcessException(exitValue, "Error " + process.exitValue() + "\nCommand " + Arrays.toString(command) + "\nLogs: " + errorFile.getName());
                } else if (exitValue != 0) {
                    LOGGER.error("Completed with strange exit code({}, {}, {})", exitValue, Arrays.toString(command), errorFile.getName());
                }

                String result = null;
                if (returnResult && StringUtils.isNotBlank(outputRedirectFile)) {
                    result = FileUtils.readFileToString(new File(outputRedirectFile), StandardCharsets.UTF_8);
                }

                FileUtils.deleteQuietly(errorFile);
                return result;
            } finally {
                process.destroy();
            }
        } catch (ProcessTimedOutException | InterruptedException e) {
            throw e;
        } catch (Exception ex) {
            if (ex instanceof ProcessException) {
                throw (ProcessException) ex;
            } else {
                throw new ProcessException(ex);
            }
        }
    }

    public File getErrorLogFile() {
        try {
            return File.createTempFile("log", ".txt", new File(processLoggingDir));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);

            throw new RuntimeException(e);
        }
    }

    private File createTempFile() {
        try {
            return File.createTempFile("tmp", ".txt");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);

            throw new RuntimeException(e);
        }
    }
}
