package ru.gadjini.telegram.smart.bot.commons.service.file.temp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;
import ru.gadjini.telegram.smart.bot.commons.utils.SmartFileUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TempFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileService.class);

    private Map<FileTarget, TempDirectoryService> fileTypeServices = new HashMap<>();

    @Value("${temp.dir:#{systemProperties['java.io.tmpdir']}}")
    private String tempDir;

    @Value("${downloads.temp.dir:#{systemProperties['java.io.tmpdir']}}")
    private String downloadsTempDir;

    @Value("${uploads.temp.dir:#{systemProperties['java.io.tmpdir']}}")
    private String uploadsTempDir;

    private ServerProperties serverProperties;

    private BotProperties botProperties;

    @Autowired
    public TempFileService(ServerProperties serverProperties, BotProperties botProperties) {
        this.serverProperties = serverProperties;
        this.botProperties = botProperties;
    }

    @PostConstruct
    public void init() {
        tempDir = mkdirsAndGet(tempDir, botProperties.getName());
        downloadsTempDir = mkdirsAndGet(downloadsTempDir, botProperties.getName());
        uploadsTempDir = mkdirsAndGet(uploadsTempDir, botProperties.getName());

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

    public void delete(SmartTempFile file) {
        if (isRemoteFile(file.getAbsolutePath())) {
            deleteRemoteFile(file);
        }

        file.smartDelete();
    }

    private void deleteRemoteFile(SmartTempFile file) {
        String filePath = file.getAbsolutePath();
        String script = "scripts/delete_remote_file.sh";
        if (file.isDirectory()) {
            script = "scripts/delete_remote_dir.sh";
        }
        ProcessBuilder processBuilder = new ProcessBuilder("sh", script, filePath);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);

        try {
            Process start = processBuilder.start();
            start.waitFor(5, TimeUnit.SECONDS);

            LOGGER.debug("Delete remote file({},{})", start.exitValue(), filePath);
        } catch (Throwable e) {
            LOGGER.error("Error delete remote file " + filePath + "\n" + e.getMessage(), e);
        }
    }

    private boolean isRemoteFile(String filePath) {
        return serverProperties.getNumber() != SmartBotConfiguration.PRIMARY_SERVER_NUMBER && filePath.startsWith(downloadsTempDir);
    }

    private String mkdirsAndGet(String parent, String child) {
        File file = new File(parent, child);
        SmartFileUtils.mkdirs(file);

        return file.getAbsolutePath();
    }
}
