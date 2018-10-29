package io.github.dhamith93.projectone;

public class Project {
    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    private String active;

    public Project() { }

    public Project(String active) {
        this.active = active;
    }
}
