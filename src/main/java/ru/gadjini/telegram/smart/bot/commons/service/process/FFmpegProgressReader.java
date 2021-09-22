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

    private static final String TIME_GROUP = "time";

    private static final String SPEED_GROUP = "speed";

    private static final Pattern TIME_SPEED_PATTERN = Pattern.compile("(?<time>(?<=time=)[\\d:.]*).*(?<speed>(?<=speed=)[\\d:.]*)x");

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
                LOGGER.debug("Progress duration null");
                return;
            }
            String line = getLastLine();
            if (StringUtils.isBlank(line)) {
                LOGGER.debug("Progress empty line({})", file.getAbsolutePath());
                return;
            }

            Matcher matcher = TIME_SPEED_PATTERN.matcher(line);
            if (matcher.find()) {
                String match = line.substring(matcher.start(TIME_GROUP), matcher.end(TIME_GROUP));
                String[] matchSplit = match.split(":");

                double passedTime = Integer.parseInt(matchSplit[0]) * 3600 +
                        Integer.parseInt(matchSplit[1]) * 60 +
                        Double.parseDouble(matchSplit[2]);

                int progress = getProgressPercentage(passedTime, dur.intValue());

                Double speed = null;
                try {
                    speed = Double.parseDouble(line.substring(matcher.start(SPEED_GROUP), matcher.end(SPEED_GROUP)));
                } catch (Throwable e) {
                    LOGGER.error("Failed parse speed({})", e.getMessage());
                }
                progressCallback.progress(dur.intValue() - (int) passedTime, progress, speed);
            }
        } catch (Throwable e) {
            LOGGER.error("Error update progress({}, {})", e.getMessage(), file.getAbsolutePath());
        }
    }

    private int getProgressPercentage(double passedTime, int dur) {
        double progress = passedTime / dur;

        return (int) (progress * 100);
    }

    private String getLastLine() {
        if (!file.exists()) {
            LOGGER.debug("Progress file deleted({})", file.getAbsolutePath());
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
