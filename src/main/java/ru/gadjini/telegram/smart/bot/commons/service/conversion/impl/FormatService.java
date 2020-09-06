package ru.gadjini.telegram.smart.bot.commons.service.conversion.impl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.service.conversion.api.Format;
import ru.gadjini.telegram.smart.bot.commons.utils.MimeTypeUtils;

@Service
public class FormatService {

    public String getExt(String fileName, String mimeType) {
        String extension = MimeTypeUtils.getExtension(mimeType);

        if (StringUtils.isNotBlank(extension) && !".bin".equals(extension)) {
            extension = extension.substring(1);
            if (extension.equals("mpga")) {
                return "mp3";
            }
        } else {
            extension = FilenameUtils.getExtension(fileName);
        }

        if ("jpeg".equals(extension)) {
            return "jpg";
        }

        return StringUtils.isBlank(extension) ? "bin" : extension;
    }

    public Format getFormat(String fileName, String mimeType) {
        String extension = getExt(fileName, mimeType);
        if (StringUtils.isBlank(extension)) {
            return null;
        }

        for (Format format : Format.values()) {
            if (format.getExt().equals(extension)) {
                return format;
            }
        }

        return null;
    }
}
