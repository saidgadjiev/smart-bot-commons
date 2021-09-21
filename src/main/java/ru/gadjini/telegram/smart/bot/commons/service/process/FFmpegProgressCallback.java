package ru.gadjini.telegram.smart.bot.commons.service.process;

public interface FFmpegProgressCallback {

    void progress(int percentage);

    Long duration();
}
