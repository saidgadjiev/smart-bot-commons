package ru.gadjini.telegram.smart.bot.commons.service.process;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FFmpegProgressReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegProgressReader.class);

    private static final Pattern TIME_PATTERN = Pattern.compile("(?<=time=)[\\d:.]*");

    private File file;

    private FFmpegProgressCallback progressCallback;

    public FFmpegProgressReader(File file, FFmpegProgressCallback progressCallback) {
        this.file = file;
        this.progressCallback = progressCallback;
    }

    public void readProgress() {
        try {
            Long dur = progressCallback.duration();
            if (dur == null) {
                LOGGER.debug("Duration null");
                return;
            }
            String line = getLastLine();
            if (StringUtils.isBlank(line)) {
                return;
            }

            Matcher matcher = TIME_PATTERN.matcher(line);
            if (matcher.find()) {
                String match = line.substring(matcher.start(), matcher.end());
                int progress = getProgressPercentage(match, dur.intValue());

                progressCallback.progress(progress);
            }
        } catch (Throwable e) {
            LOGGER.error("Error update progress({})", e.getMessage());
        }
    }

    private int getProgressPercentage(String match, int dur) {
        String[] matchSplit = match.split(":");

        double progress = (Integer.parseInt(matchSplit[0]) * 3600 +
                Integer.parseInt(matchSplit[1]) * 60 +
                Double.parseDouble(matchSplit[2])) / dur;

        return (int) (progress * 100);
    }

    private String getLastLine() {
        if (!file.exists()) {
            return null;
        }
        try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8)) {
            return reversedLinesFileReader.readLine();
        } catch (Exception e) {
            LOGGER.error("Error reading progress last line({})", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
