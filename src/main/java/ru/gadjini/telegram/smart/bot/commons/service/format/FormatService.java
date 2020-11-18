package ru.gadjini.telegram.smart.bot.commons.service.format;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.utils.MimeTypeUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.UrlUtils;

import static ru.gadjini.telegram.smart.bot.commons.service.format.Format.TEXT;
import static ru.gadjini.telegram.smart.bot.commons.service.format.Format.URL;

@Service
public class FormatService {

    public Format getAssociatedFormat(String format) {
        if ("jpeg".equals(format)) {
            return Format.JPG;
        }
        format = format.toUpperCase();
        for (Format f : Format.values()) {
            if (f.getName().equals(format)) {
                return f;
            }
        }

        return null;
    }

    public String getExt(String fileName, String mimeType) {
        String extension = MimeTypeUtils.getExtension(fileName, mimeType);

        if (StringUtils.isNotBlank(extension) && !".bin".equals(extension)) {
            extension = extension.substring(1);
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

    public Format getFormat(String text) {
        if (UrlUtils.isUrl(text)) {
            return URL;
        }

        return TEXT;
    }
}
