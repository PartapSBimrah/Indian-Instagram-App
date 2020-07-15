package com.royalbimrah.likwik.Model;

public class Story {
    private String imageUrl;
    private String userId;
    private String storyId;
    private long startTime;
    private long endTime;

    public Story(String imageUrl, String userId, String storyId, long startTime, long endTime) {
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.storyId = storyId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Story() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

}
