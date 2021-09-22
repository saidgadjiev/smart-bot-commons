package ru.gadjini.telegram.smart.bot.commons.service.process;

public interface FFmpegProgressCallback {

    void progress(int timeLeft, int percentage, Double speed);

    Long duration();
}
