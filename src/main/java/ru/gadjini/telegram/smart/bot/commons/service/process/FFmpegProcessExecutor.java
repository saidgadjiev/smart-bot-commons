package ru.gadjini.telegram.smart.bot.commons.service.process;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.exception.ProcessException;
import ru.gadjini.telegram.smart.bot.commons.exception.ProcessTimedOutException;
import ru.gadjini.telegram.smart.bot.commons.property.ProgressProperties;
import ru.gadjini.telegram.smart.bot.commons.service.ProcessExecutor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class FFmpegProcessExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegProcessExecutor.class);

    private ProcessExecutor processExecutor;

    private List<FFmpegProgressReader> scanners = Collections.synchronizedList(new ArrayList<>());

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ProgressProperties progressProperties;

    @Autowired
    public FFmpegProcessExecutor(ProcessExecutor processExecutor, ProgressProperties progressProperties) {
        this.processExecutor = processExecutor;
        this.progressProperties = progressProperties;
    }

    public int scannersCount() {
        return scanners.size();
    }

    @Scheduled(fixedDelay = 2 * 1000)
    public void progressReader() {
        if (progressProperties.isDisabled()) {
            return;
        }
        executorService.submit(() -> {
            for (FFmpegProgressReader scanner : new ArrayList<>(scanners)) {
                scanner.readProgress();
            }
        });
    }

    public void execute(String[] command, FFmpegProgressCallback progressCallback) throws InterruptedException {
        File errorFile = processExecutor.getErrorLogFile();
        try {
            FileUtils.writeStringToFile(errorFile, String.join(" ", command) + "\n", StandardCharsets.UTF_8);
            ProcessBuilder processBuilder = new ProcessBuilder(command);

            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(errorFile));

            Process process = processBuilder.start();
            FFmpegProgressReader consoleReader = null;
            if (!progressProperties.isDisabled()) {
                consoleReader = new FFmpegProgressReader(errorFile, progressCallback);
                scanners.add(consoleReader);
            }
            try {
                int exitValue = process.waitFor();

                if (!Set.of(139, 0).contains(exitValue)) {
                    LOGGER.error("Error({}, {}, {})", process.exitValue(), Arrays.toString(command), errorFile.getName());
                    throw new ProcessException(exitValue, "Error " + process.exitValue() +
                            "\nCommand " + Arrays.toString(command) + "\nLogs: " + errorFile.getName());
                }
            } finally {
                process.destroy();
                if (consoleReader != null) {
                    scanners.remove(consoleReader);
                }
            }

            FileUtils.deleteQuietly(errorFile);
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
}
