package io.github.dhamith93.projectone.pojo;

public class Notification {
    private String title;
    private String from;
    private String groupName;
    private String groupId;
    private String seen;
    private String type;

    public Notification() { }

    public Notification(String title, String from, String groupName, String groupId, String seen, String type) {
        this.title = title;
        this.from = from;
        this.groupName = groupName;
        this.groupId = groupId;
        this.seen = seen;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
