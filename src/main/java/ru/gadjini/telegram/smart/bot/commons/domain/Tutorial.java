package ru.gadjini.telegram.smart.bot.commons.domain;

public class Tutorial {

    public static final String ID = "id";

    public static final String DESCRIPTION = "description";

    public static final String COMMAND = "cmd";

    public static final String FILE_ID = "file_id";

    private int id;

    private String description;

    private String command;

    private String fileId;

    private String botName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }
}
