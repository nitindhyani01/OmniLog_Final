package com.omnilog.model;

public class MediaItem {
    private String id;
    private String title;
    private String creator;       // author/director
    private String mediaType;     // BOOK, MOVIE, TV_SHOW, GAME, etc.
    private String genreOrTopic;
    private String coverUrl;
    private String description;
    private int releaseYear;
    private double averageRating;
    private int ratingCount;
    private int reviewCount;
    private int watchlistCount;
    private String addedBy;
    private String addedDate;

    public MediaItem() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getGenreOrTopic() { return genreOrTopic; }
    public void setGenreOrTopic(String genreOrTopic) { this.genreOrTopic = genreOrTopic; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public int getWatchlistCount() { return watchlistCount; }
    public void setWatchlistCount(int watchlistCount) { this.watchlistCount = watchlistCount; }
    public String getAddedBy() { return addedBy; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }
    public String getAddedDate() { return addedDate; }
    public void setAddedDate(String addedDate) { this.addedDate = addedDate; }
}