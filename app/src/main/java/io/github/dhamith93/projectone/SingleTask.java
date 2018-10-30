package io.github.dhamith93.projectone;

public class SingleTask {
    private String name;
    private String desc;
    private String member;
    private String status;

    public SingleTask() {  }

    public SingleTask(String name, String desc, String member, String status) {
        this.name = name;
        this.desc = desc;
        this.member = member;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
