package com.royalbimrah.likwik.Model;

public class User {
    private String id;
    private String username;
    private String fullName;
    private String imageUrl;
    private String bio;

    public User(String id, String username, String fullName, String imageUrl, String bio) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.imageUrl = imageUrl;
        this.bio = bio;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
