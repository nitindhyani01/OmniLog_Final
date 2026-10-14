package com.omnilog.model;

import java.util.List;

public class Review {
    private String id;
    private String userId;
    private String username;
    private String userAvatar;
    private String mediaId;
    private String mediaTitle;
    private String mediaCover;
    private String mediaType;
    private int rating;           // 1-5 (or 0.5-5 for Letterboxd style)
    private String reviewText;
    private String status;        // WATCHED, WATCHING, WANT_TO_WATCH, DROPPED
    private boolean containsSpoilers;
    private int likeCount;
    private List<String> likedBy;
    private String loggedDate;
    private String updatedDate;
    private boolean rewatch;

    public Review() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }
    public String getMediaTitle() { return mediaTitle; }
    public void setMediaTitle(String mediaTitle) { this.mediaTitle = mediaTitle; }
    public String getMediaCover() { return mediaCover; }
    public void setMediaCover(String mediaCover) { this.mediaCover = mediaCover; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isContainsSpoilers() { return containsSpoilers; }
    public void setContainsSpoilers(boolean containsSpoilers) { this.containsSpoilers = containsSpoilers; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public List<String> getLikedBy() { return likedBy; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }
    public String getLoggedDate() { return loggedDate; }
    public void setLoggedDate(String loggedDate) { this.loggedDate = loggedDate; }
    public String getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(String updatedDate) { this.updatedDate = updatedDate; }
    public boolean isRewatch() { return rewatch; }
    public void setRewatch(boolean rewatch) { this.rewatch = rewatch; }
}