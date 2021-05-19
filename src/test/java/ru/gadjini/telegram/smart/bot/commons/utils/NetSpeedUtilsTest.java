package ru.gadjini.telegram.smart.bot.commons.utils;

import org.junit.Assert;
import org.junit.Test;

public class NetSpeedUtilsTest {

    @Test
    public void toSpeed() {
        String s = NetSpeedUtils.toSpeed(23442342);

        Assert.assertEquals("22 MB/s", s);
    }
}