package ru.gadjini.telegram.smart.bot.commons.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

public class MimeTypeUtils {

    private MimeTypeUtils() {
    }

    public static String getExtension(String fileName, String mimeType) {
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

        String ext = FilenameUtils.getExtension(fileName);
        if (StringUtils.isBlank(ext)) {
            return parsedMimeType.getExtension();
        } else {
            ext = "." + ext;
            return parsedMimeType.getExtensions().contains(ext) ? ext : parsedMimeType.getExtension();
        }
    }
}
