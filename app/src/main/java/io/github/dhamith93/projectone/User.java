package io.github.dhamith93.projectone;

public class User {
    private String name;
    private String profile_pic;

    public User() { }

    public User(String name, String profile_pic) {
        this.name = name;
        this.profile_pic = profile_pic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }
}
