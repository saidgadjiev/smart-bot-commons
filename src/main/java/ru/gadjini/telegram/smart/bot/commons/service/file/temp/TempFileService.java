package ru.gadjini.telegram.smart.bot.commons.service.file.temp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class TempFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileService.class);

    private Map<FileTarget, TempDirectoryService> fileTypeServices = new HashMap<>();

    @Value("${temp.dir:#{systemProperties['java.io.tmpdir']}}")
    private String tempDir;

    @Value("${download.temp.dir:#{systemProperties['java.io.tmpdir']}}")
    private String downloadsTempDir;

    @Value("${upload.temp.dir:#{systemProperties['java.io.tmpdir']}}")
    private String uploadsTempDir;

    @PostConstruct
    public void init() {
        LOGGER.debug("Temp dir({},{},{})", tempDir, downloadsTempDir, uploadsTempDir);
        fileTypeServices.put(FileTarget.TEMP, new TempDirectoryService(tempDir));
        fileTypeServices.put(FileTarget.DOWNLOAD, new TempDirectoryService(downloadsTempDir));
        fileTypeServices.put(FileTarget.UPLOAD, new TempDirectoryService(uploadsTempDir));
    }

    public String getRootDir(FileTarget tempFileType) {
        return fileTypeServices.get(tempFileType).getRootDir();
    }

    public SmartTempFile createTempDir(FileTarget tempFileType, long chatId, String tag) {
        return fileTypeServices.get(tempFileType).createTempDir(chatId, tag);
    }

    public String getTempDir(FileTarget tempFileType, long chatId, String tag) {
        return fileTypeServices.get(tempFileType).getTempDir(chatId, tag);
    }

    public SmartTempFile getTempFile(FileTarget tempFileType, long chatId, String fileId, String tag, String ext) {
        return fileTypeServices.get(tempFileType).getTempFile(chatId, fileId, tag, ext);
    }

    public SmartTempFile getTempFile(FileTarget tempFileType, long chatId, String tag, String ext) {
        return getTempFile(tempFileType, chatId, null, tag, ext);
    }

    public String getTempFile(FileTarget tempFileType, String parent, long chatId, String fileId, String tag, String ext) {
        return fileTypeServices.get(tempFileType).getTempFile(parent, chatId, fileId, tag, ext);
    }

    public SmartTempFile createTempFile(FileTarget tempFileType, SmartTempFile parent, long chatId, String fileName) {
        return fileTypeServices.get(tempFileType).createTempFile(parent, chatId, fileName);
    }

    public SmartTempFile createTempFile(FileTarget tempFileType, SmartTempFile parent, long chatId, String fileId, String tag, String ext) {
        return fileTypeServices.get(tempFileType).createTempFile(parent, chatId, fileId, tag, ext);
    }

    public SmartTempFile createTempFile(FileTarget tempFileType, long chatId, String fileId, String tag, String ext) {
        return fileTypeServices.get(tempFileType).createTempFile(chatId, fileId, tag, ext);
    }

    public SmartTempFile createTempFile(FileTarget tempFileType, long chatId, String tag, String ext) {
        return createTempFile(tempFileType, chatId, null, tag, ext);
    }

    public SmartTempFile createTempFile(FileTarget tempFileType, String tag, String ext) {
        return createTempFile(tempFileType, 0, null, tag, ext);
    }
}
