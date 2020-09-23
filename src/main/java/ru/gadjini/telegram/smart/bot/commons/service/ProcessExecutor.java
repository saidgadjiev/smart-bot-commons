package ru.gadjini.telegram.smart.bot.commons.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gadjini.telegram.smart.bot.commons.exception.ProcessException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ProcessExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutor.class);

    public String executeWithResult(String[] command) {
        return execute(command, ProcessBuilder.Redirect.PIPE, null, null);
    }

    public void executeWithFile(String[] command, String outputFile) {
        execute(command, null, outputFile, null);
    }

    public void executeWithRedirectError(String[] command, String errorFile) {
        execute(command, null, errorFile, errorFile);
    }

    public void execute(String[] command) {
        execute(command, ProcessBuilder.Redirect.DISCARD, null, null);
    }

    private String execute(String[] command, ProcessBuilder.Redirect redirectOutput, String outputRedirectFile,  String errorRedirectFile) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            if (redirectOutput != null) {
                processBuilder.redirectOutput(redirectOutput);
            } else if (StringUtils.isNotBlank(outputRedirectFile)) {
                processBuilder.redirectOutput(new File(outputRedirectFile));
            }
            if (StringUtils.isNotBlank(errorRedirectFile)) {
                processBuilder.redirectError(new File(errorRedirectFile));
            }
            Process process = processBuilder.start();
            try {
                int exitValue = process.waitFor();
                if (exitValue != 0) {
                    String error = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
                    error += "\n" + IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);

                    LOGGER.error("Error({}, {}, {})", process.exitValue(), Arrays.toString(command), error);
                    throw new ProcessException("Error " + process.exitValue() + "\nCommand " + Arrays.toString(command) + "\n" + error);
                }

                if (redirectOutput == ProcessBuilder.Redirect.PIPE) {
                    return IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
                }

                return null;
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
}
