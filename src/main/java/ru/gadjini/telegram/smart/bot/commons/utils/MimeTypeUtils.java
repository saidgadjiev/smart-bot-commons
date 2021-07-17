package ru.gadjini.telegram.smart.bot.commons.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MimeTypeUtils {

    public static final Pattern CS_PATTERN = Pattern.compile(";charset=.*");

    private MimeTypeUtils() {
    }

    public static String removeCharset(String mime) {
        Matcher matcher = CS_PATTERN.matcher(mime);
        if (matcher.find()) {
            return matcher.replaceFirst("");
        }

        return mime;
    }

    public static String getExtension(String fileNameExtension, String mimeType) {
        if (mimeType == null) {
            return null;
        }
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes(MimeTypeUtils.class.getClassLoader());
        MimeType parsedMimeType;
        try {
            parsedMimeType = allTypes.forName(mimeType);
        } catch (MimeTypeException e) {
            return null;
        }

        if (StringUtils.isBlank(fileNameExtension)) {
            return parsedMimeType.getExtension();
        } else {
            fileNameExtension = "." + fileNameExtension;
            return parsedMimeType.getExtensions().contains(fileNameExtension) ? fileNameExtension : parsedMimeType.getExtension();
        }
    }
}
