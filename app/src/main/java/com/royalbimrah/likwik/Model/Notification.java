package com.royalbimrah.likwik.Model;

public class Notification {
    private String userId;
    private String note;
    private String posterId;
    private String postId;
    private boolean isPost;
    private String commentId;

    public Notification(String userId, String note, String posterId, String postId, boolean isPost, String commentId) {
        this.userId = userId;
        this.note = note;
        this.posterId = posterId;
        this.postId = postId;
        this.isPost = isPost;
        this.commentId = commentId;
    }

    public Notification() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public boolean getIsPost() {
        return isPost;
    }

    public void setIsPost(boolean post) {
        isPost = post;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
}
