package com.omnilog.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Filters.regex;
import com.mongodb.client.model.Sorts;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;
import com.omnilog.model.MediaItem;
import com.omnilog.model.Review;
import com.omnilog.util.DBConnection;

public class MediaDAO {

    // Fallback stores
    private static final Map<String, MediaItem> fallbackMedia = new ConcurrentHashMap<>();
    private static final Map<String, Review> fallbackReviews = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> fallbackWatchlists = new ConcurrentHashMap<>(); // userId -> itemIds
    private static final Map<String, Set<String>> fallbackLikes = new ConcurrentHashMap<>();     // userId -> reviewIds

    static {
        seedFallback("Clean Code", "Robert C. Martin", "BOOK", "Software Engineering",
                "https://images.unsplash.com/photo-1532012164546-f432f2e3777f?w=400", 2008,
                "A handbook of agile software craftsmanship.");
        seedFallback("Oppenheimer", "Christopher Nolan", "MOVIE", "Biographical Drama",
                "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=400", 2023,
                "The story of American scientist J. Robert Oppenheimer.");
        seedFallback("Attention Is All You Need", "Vaswani et al.", "RESEARCH_PAPER", "Deep Learning",
                "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400", 2017,
                "The paper that introduced the Transformer architecture.");
        seedFallback("Reflections on Trusting Trust", "Ken Thompson", "ARTICLE", "Cybersecurity",
                "https://images.unsplash.com/photo-1505373877841-8d25f7d46678?w=400", 1984,
                "A classic Turing Award lecture on trust in software.");
        seedFallback("The Pragmatic Programmer", "Hunt & Thomas", "BOOK", "Software Engineering",
                "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400", 1999,
                "Your journey to mastery.");
        seedFallback("Interstellar", "Christopher Nolan", "MOVIE", "Sci-Fi",
                "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=400", 2014,
                "A team travels through a wormhole in search of a new home.");
    }

    private static void seedFallback(String title, String creator, String type, String topic,
                                     String cover, int year, String desc) {
        MediaItem m = new MediaItem();
        m.setId("media_" + UUID.randomUUID().toString().substring(0, 8));
        m.setTitle(title);
        m.setCreator(creator);
        m.setMediaType(type);
        m.setGenreOrTopic(topic);
        m.setCoverUrl(cover);
        m.setReleaseYear(year);
        m.setDescription(desc);
        m.setAddedDate(LocalDate.now().toString());
        fallbackMedia.put(m.getId(), m);
    }

    // ---------- MEDIA ----------

    public List<MediaItem> getAllMedia() {
        return getAllMedia(null, null, null);
    }

    public List<MediaItem> getAllMedia(String mediaTypeFilter, String searchQuery, String sortBy) {
        List<MediaItem> list = new ArrayList<>();
        if (DBConnection.isMongoAvailable()) {
            try {
                MongoCollection<Document> col = DBConnection.getDatabase().getCollection("media_items");
                List<org.bson.conversions.Bson> filters = new ArrayList<>();
                if (mediaTypeFilter != null && !mediaTypeFilter.isBlank())
                    filters.add(eq("media_type", mediaTypeFilter));
                if (searchQuery != null && !searchQuery.isBlank())
                    filters.add(or(regex("title", searchQuery, "i"), regex("creator", searchQuery, "i")));
                var find = filters.isEmpty() ? col.find() : col.find(and(filters));
                if ("rating".equals(sortBy)) find = find.sort(Sorts.descending("average_rating"));
                else if ("title".equals(sortBy)) find = find.sort(Sorts.ascending("title"));
                else find = find.sort(Sorts.descending("_id"));
                for (Document doc : find) list.add(docToMedia(doc));
                return list;
            } catch (Exception e) { e.printStackTrace(); }
        }
        // Fallback
        return fallbackMedia.values().stream()
            .filter(m -> mediaTypeFilter == null || mediaTypeFilter.isBlank()
                       || m.getMediaType().equals(mediaTypeFilter))
            .filter(m -> searchQuery == null || searchQuery.isBlank()
                       || m.getTitle().toLowerCase().contains(searchQuery.toLowerCase())
                       || m.getCreator().toLowerCase().contains(searchQuery.toLowerCase()))
            .sorted((a, b) -> {
                if ("rating".equals(sortBy)) return Double.compare(b.getAverageRating(), a.getAverageRating());
                if ("title".equals(sortBy)) return a.getTitle().compareToIgnoreCase(b.getTitle());
                return b.getId().compareTo(a.getId());
            })
            .collect(Collectors.toList());
    }

    public MediaItem getMediaById(String id) {
        if (DBConnection.isMongoAvailable()) {
            try {
                Document doc = null;
                try { doc = DBConnection.getDatabase().getCollection("media_items")
                    .find(eq("_id", new ObjectId(id))).first(); }
                catch (IllegalArgumentException ignored) {}
                if (doc != null) return docToMedia(doc);
            } catch (Exception e) { e.printStackTrace(); }
        }
        return fallbackMedia.get(id);
    }

    public boolean addCustomMedia(String title, String creator, String mediaType, String topic,
                                  String coverUrl, int year, String description, String addedBy) {
        String finalCover = (coverUrl != null && !coverUrl.trim().isEmpty())
                ? coverUrl : "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400";

        if (DBConnection.isMongoAvailable()) {
            try {
                Document doc = new Document("title", title)
                    .append("creator", creator)
                    .append("media_type", mediaType)
                    .append("genre_or_topic", topic)
                    .append("cover_url", finalCover)
                    .append("description", description)
                    .append("release_year", year)
                    .append("average_rating", 0.0)
                    .append("rating_count", 0)
                    .append("review_count", 0)
                    .append("watchlist_count", 0)
                    .append("added_by", addedBy)
                    .append("added_date", LocalDate.now().toString());
                DBConnection.getDatabase().getCollection("media_items").insertOne(doc);
                return true;
            } catch (Exception e) { e.printStackTrace(); }
        }

        MediaItem m = new MediaItem();
        m.setId("media_" + UUID.randomUUID().toString().substring(0, 8));
        m.setTitle(title);
        m.setCreator(creator);
        m.setMediaType(mediaType);
        m.setGenreOrTopic(topic);
        m.setCoverUrl(finalCover);
        m.setDescription(description);
        m.setReleaseYear(year);
        m.setAddedBy(addedBy);
        m.setAddedDate(LocalDate.now().toString());
        fallbackMedia.put(m.getId(), m);
        return true;
    }

    // ---------- REVIEWS / LOGS ----------

    public boolean logItem(String userId, String username, String userAvatar, String itemId,
                           int rating, String review, String status, boolean spoilers, boolean rewatch) {
        MediaItem item = getMediaById(itemId);
        if (item == null) return false;

        if (DBConnection.isMongoAvailable()) {
            try {
                MongoDatabase db = DBConnection.getDatabase();
                MongoCollection<Document> logs = db.getCollection("user_logs");
                Document existing = logs.find(and(eq("user_id", userId), eq("item_id", itemId))).first();
                if (existing != null) {
                    logs.updateOne(eq("_id", existing.getObjectId("_id")), combine(
                        set("rating", rating), set("review_text", review),
                        set("status", status), set("contains_spoilers", spoilers),
                        set("rewatch", rewatch),
                        set("updated_date", LocalDate.now().toString())));
                    recalcMediaStats(itemId);
                    return true;
                }
                Document doc = new Document("user_id", userId)
                    .append("username", username)
                    .append("user_avatar", userAvatar)
                    .append("item_id", itemId)
                    .append("title", item.getTitle())
                    .append("creator", item.getCreator())
                    .append("media_type", item.getMediaType())
                    .append("cover_url", item.getCoverUrl())
                    .append("rating", rating)
                    .append("review_text", review)
                    .append("status", status)
                    .append("contains_spoilers", spoilers)
                    .append("rewatch", rewatch)
                    .append("like_count", 0)
                    .append("liked_by", new ArrayList<String>())
                    .append("logged_date", LocalDate.now().toString())
                    .append("updated_date", LocalDate.now().toString());
                logs.insertOne(doc);
                recalcMediaStats(itemId);
                return true;
            } catch (Exception e) { e.printStackTrace(); }
        }

        // Fallback
        Review r = new Review();
        r.setId("review_" + UUID.randomUUID().toString().substring(0, 8));
        r.setUserId(userId);
        r.setUsername(username);
        r.setUserAvatar(userAvatar);
        r.setMediaId(itemId);
        r.setMediaTitle(item.getTitle());
        r.setMediaCover(item.getCoverUrl());
        r.setMediaType(item.getMediaType());
        r.setRating(rating);
        r.setReviewText(review);
        r.setStatus(status);
        r.setContainsSpoilers(spoilers);
        r.setRewatch(rewatch);
        r.setLoggedDate(LocalDate.now().toString());
        r.setUpdatedDate(LocalDate.now().toString());
        r.setLikedBy(new ArrayList<>());
        fallbackReviews.put(r.getId(), r);
        recalcFallbackStats(itemId);
        return true;
    }

    public boolean updateLog(String userId, String logId, int rating, String review,
                             String status, boolean spoilers) {
        if (DBConnection.isMongoAvailable()) {
            try {
                MongoCollection<Document> logs = DBConnection.getDatabase().getCollection("user_logs");
                Document existing = null;
                try { existing = logs.find(and(eq("_id", new ObjectId(logId)), eq("user_id", userId))).first(); }
                catch (IllegalArgumentException ignored) {}
                if (existing == null) return false;
                logs.updateOne(eq("_id", existing.getObjectId("_id")), combine(
                    set("rating", rating), set("review_text", review),
                    set("status", status), set("contains_spoilers", spoilers),
                    set("updated_date", LocalDate.now().toString())));
                recalcMediaStats(existing.getString("item_id"));
                return true;
            } catch (Exception e) { e.printStackTrace(); }
        }
        Review r = fallbackReviews.get(logId);
        if (r != null && r.getUserId().equals(userId)) {
            r.setRating(rating);
            r.setReviewText(review);
            r.setStatus(status);
            r.setContainsSpoilers(spoilers);
            r.setUpdatedDate(LocalDate.now().toString());
            recalcFallbackStats(r.getMediaId());
            return true;
        }
        return false;
    }

    public boolean deleteLog(String userId, String logId) {
        if (DBConnection.isMongoAvailable()) {
            try {
                MongoCollection<Document> logs = DBConnection.getDatabase().getCollection("user_logs");
                Document existing = null;
                try { existing = logs.find(and(eq("_id", new ObjectId(logId)), eq("user_id", userId))).first(); }
                catch (IllegalArgumentException ignored) {}
                if (existing == null) return false;
                String itemId = existing.getString("item_id");
                logs.deleteOne(eq("_id", existing.getObjectId("_id")));
                recalcMediaStats(itemId);
                return true;
            } catch (Exception e) { e.printStackTrace(); }
        }
        Review r = fallbackReviews.get(logId);
        if (r != null && r.getUserId().equals(userId)) {
            fallbackReviews.remove(logId);
            recalcFallbackStats(r.getMediaId());
            return true;
        }
        return false;
    }

    public List<Review> getUserDiary(String userId) {
        if (DBConnection.isMongoAvailable()) {
            try {
                List<Review> list = new ArrayList<>();
                for (Document doc : DBConnection.getDatabase().getCollection("user_logs")
                        .find(eq("user_id", userId)).sort(Sorts.descending("logged_date"))) {
                    list.add(docToReview(doc));
                }
                return list;
            } catch (Exception e) { e.printStackTrace(); }
        }
        return fallbackReviews.values().stream()
            .filter(r -> r.getUserId().equals(userId))
            .sorted((a, b) -> b.getLoggedDate().compareTo(a.getLoggedDate()))
            .collect(Collectors.toList());
    }

    public List<Review> getReviewsForMedia(String mediaId) {
        if (DBConnection.isMongoAvailable()) {
            try {
                List<Review> list = new ArrayList<>();
                for (Document doc : DBConnection.getDatabase().getCollection("user_logs")
                        .find(eq("item_id", mediaId)).sort(Sorts.descending("logged_date"))) {
                    list.add(docToReview(doc));
                }
                return list;
            } catch (Exception e) { e.printStackTrace(); }
        }
        return fallbackReviews.values().stream()
            .filter(r -> r.getMediaId().equals(mediaId))
            .sorted((a, b) -> b.getLoggedDate().compareTo(a.getLoggedDate()))
            .collect(Collectors.toList());
    }

    public List<Review> getCommunityActivity(int limit) {
        if (DBConnection.isMongoAvailable()) {
            try {
                List<Review> list = new ArrayList<>();
                for (Document doc : DBConnection.getDatabase().getCollection("user_logs")
                        .find().sort(Sorts.descending("logged_date")).limit(limit)) {
                    list.add(docToReview(doc));
                }
                return list;
            } catch (Exception e) { e.printStackTrace(); }
        }
        return fallbackReviews.values().stream()
            .sorted((a, b) -> b.getLoggedDate().compareTo(a.getLoggedDate()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    public boolean toggleLike(String userId, String reviewId) {
        if (DBConnection.isMongoAvailable()) {
            try {
                MongoCollection<Document> logs = DBConnection.getDatabase().getCollection("user_logs");
                Document doc = null;
                try { doc = logs.find(eq("_id", new ObjectId(reviewId))).first(); }
                catch (IllegalArgumentException ignored) {}
                if (doc == null) return false;
                List<String> likedBy = doc.getList("liked_by", String.class);
                if (likedBy == null) likedBy = new ArrayList<>();
                boolean liked;
                if (likedBy.contains(userId)) { likedBy.remove(userId); liked = false; }
                else { likedBy.add(userId); liked = true; }
                logs.updateOne(eq("_id", doc.getObjectId("_id")), combine(
                    set("liked_by", likedBy), set("like_count", likedBy.size())));
                return liked;
            } catch (Exception e) { e.printStackTrace(); }
        }
        Review r = fallbackReviews.get(reviewId);
        if (r == null) return false;
        if (r.getLikedBy() == null) r.setLikedBy(new ArrayList<>());
        if (r.getLikedBy().contains(userId)) { r.getLikedBy().remove(userId); return false; }
        r.getLikedBy().add(userId);
        r.setLikeCount(r.getLikedBy().size());
        return true;
    }

    // ---------- WATCHLIST ----------

    public boolean addToWatchlist(String userId, String itemId) {
        if (DBConnection.isMongoAvailable()) {
            try {
                MongoDatabase db = DBConnection.getDatabase();
                MongoCollection<Document> media = db.getCollection("media_items");
                MongoCollection<Document> watch = db.getCollection("watchlist");
                Document item = null;
                try { item = media.find(eq("_id", new ObjectId(itemId))).first(); }
                catch (IllegalArgumentException ignored) {}
                if (item == null) return false;
                Document existing = watch.find(and(eq("user_id", userId), eq("item_id", itemId))).first();
                if (existing != null) return true;
                watch.insertOne(new Document("user_id", userId).append("item_id", itemId)
                    .append("title", item.getString("title"))
                    .append("creator", item.getString("creator"))
                    .append("media_type", item.getString("media_type"))
                    .append("cover_url", item.getString("cover_url"))
                    .append("added_date", LocalDate.now().toString()));
                media.updateOne(eq("_id", item.getObjectId("_id")), inc("watchlist_count", 1));
                return true;
            } catch (Exception e) { e.printStackTrace(); }
        }
        MediaItem item = fallbackMedia.get(itemId);
        if (item == null) return false;
        fallbackWatchlists.computeIfAbsent(userId, k -> new HashSet<>()).add(itemId);
        item.setWatchlistCount(item.getWatchlistCount() + 1);
        return true;
    }

    public boolean removeFromWatchlist(String userId, String itemId) {
        if (DBConnection.isMongoAvailable()) {
            try {
                MongoDatabase db = DBConnection.getDatabase();
                long deleted = db.getCollection("watchlist")
                    .deleteOne(and(eq("user_id", userId), eq("item_id", itemId))).getDeletedCount();
                if (deleted > 0) {
                    try { db.getCollection("media_items")
                        .updateOne(eq("_id", new ObjectId(itemId)), inc("watchlist_count", -1)); }
                    catch (IllegalArgumentException ignored) {}
                }
                return deleted > 0;
            } catch (Exception e) { e.printStackTrace(); }
        }
        Set<String> wl = fallbackWatchlists.get(userId);
        if (wl != null && wl.remove(itemId)) {
            MediaItem m = fallbackMedia.get(itemId);
            if (m != null) m.setWatchlistCount(Math.max(0, m.getWatchlistCount() - 1));
            return true;
        }
        return false;
    }

    public List<MediaItem> getWatchlist(String userId) {
        if (DBConnection.isMongoAvailable()) {
            try {
                List<MediaItem> list = new ArrayList<>();
                for (Document doc : DBConnection.getDatabase().getCollection("watchlist")
                        .find(eq("user_id", userId)).sort(Sorts.descending("added_date"))) {
                    MediaItem m = new MediaItem();
                    m.setId(doc.getString("item_id"));
                    m.setTitle(doc.getString("title"));
                    m.setCreator(doc.getString("creator"));
                    m.setMediaType(doc.getString("media_type"));
                    m.setCoverUrl(doc.getString("cover_url"));
                    list.add(m);
                }
                return list;
            } catch (Exception e) { e.printStackTrace(); }
        }
        List<MediaItem> list = new ArrayList<>();
        Set<String> wl = fallbackWatchlists.get(userId);
        if (wl != null) for (String id : wl) {
            MediaItem m = fallbackMedia.get(id);
            if (m != null) list.add(m);
        }
        return list;
    }

    public boolean isInWatchlist(String userId, String itemId) {
        if (DBConnection.isMongoAvailable()) {
            try {
                return DBConnection.getDatabase().getCollection("watchlist")
                    .find(and(eq("user_id", userId), eq("item_id", itemId))).first() != null;
            } catch (Exception e) { e.printStackTrace(); }
        }
        Set<String> wl = fallbackWatchlists.get(userId);
        return wl != null && wl.contains(itemId);
    }

    // ---------- STATS ----------

    private void recalcMediaStats(String mediaId) {
        if (!DBConnection.isMongoAvailable()) { recalcFallbackStats(mediaId); return; }
        try {
            MongoDatabase db = DBConnection.getDatabase();
            MongoCollection<Document> logs = db.getCollection("user_logs");
            List<Document> all = logs.find(eq("item_id", mediaId)).into(new ArrayList<>());
            int count = all.size();
            double avg = 0;
            int reviewCount = 0;
            for (Document d : all) {
                Integer r = d.getInteger("rating", 0);
                avg += r;
                String txt = d.getString("review_text");
                if (txt != null && !txt.isBlank()) reviewCount++;
            }
            if (count > 0) avg = avg / count;
            try {
                db.getCollection("media_items").updateOne(eq("_id", new ObjectId(mediaId)), combine(
                    set("average_rating", Math.round(avg * 10.0) / 10.0),
                    set("rating_count", count),
                    set("review_count", reviewCount)));
            } catch (IllegalArgumentException ignored) {}
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void recalcFallbackStats(String mediaId) {
        MediaItem m = fallbackMedia.get(mediaId);
        if (m == null) return;
        int count = 0, sum = 0, reviews = 0;
        for (Review r : fallbackReviews.values()) {
            if (r.getMediaId().equals(mediaId)) {
                count++;
                sum += r.getRating();
                if (r.getReviewText() != null && !r.getReviewText().isBlank()) reviews++;
            }
        }
        m.setRatingCount(count);
        m.setReviewCount(reviews);
        m.setAverageRating(count > 0 ? Math.round((sum * 10.0) / count) / 10.0 : 0);
    }

    // ---------- CONVERTERS ----------

   private MediaItem docToMedia(Document doc) {
    MediaItem m = new MediaItem();
    m.setId(doc.getObjectId("_id").toHexString());
    m.setTitle(safeString(doc, "title", "(untitled)"));
    m.setCreator(safeString(doc, "creator", ""));
    m.setMediaType(safeString(doc, "media_type", "UNKNOWN"));
    m.setGenreOrTopic(safeString(doc, "genre_or_topic", ""));
    m.setCoverUrl(safeString(doc, "cover_url", ""));
    m.setDescription(safeString(doc, "description", ""));
    m.setReleaseYear(safeInt(doc, "release_year", 0));
    m.setAverageRating(safeDouble(doc, "average_rating", 0.0));
    m.setRatingCount(safeInt(doc, "rating_count", 0));
    m.setReviewCount(safeInt(doc, "review_count", 0));
    m.setWatchlistCount(safeInt(doc, "watchlist_count", 0));
    m.setAddedBy(safeString(doc, "added_by", ""));
    m.setAddedDate(safeString(doc, "added_date", ""));
    return m;
}

private String safeString(Document d, String k, String def) {
    try { String v = d.getString(k); return v == null ? def : v; }
    catch (Exception e) { return def; }
}
private int safeInt(Document d, String k, int def) {
    try { Integer v = d.getInteger(k); return v == null ? def : v; }
    catch (Exception e) { return def; }
}
private double safeDouble(Document d, String k, double def) {
    try { Double v = d.getDouble(k); return v == null ? def : v; }
    catch (Exception e) { return def; }
}

    private Review docToReview(Document doc) {
        Review r = new Review();
        r.setId(doc.getObjectId("_id").toHexString());
        r.setUserId(doc.getString("user_id"));
        r.setUsername(doc.getString("username"));
        r.setUserAvatar(doc.getString("user_avatar"));
        r.setMediaId(doc.getString("item_id"));
        r.setMediaTitle(doc.getString("title"));
        r.setMediaCover(doc.getString("cover_url"));
        r.setMediaType(doc.getString("media_type"));
        r.setRating(doc.getInteger("rating", 0));
        r.setReviewText(doc.getString("review_text"));
        r.setStatus(doc.getString("status"));
        r.setContainsSpoilers(doc.getBoolean("contains_spoilers", false));
        r.setRewatch(doc.getBoolean("rewatch", false));
        r.setLikeCount(doc.getInteger("like_count", 0));
        r.setLikedBy(doc.getList("liked_by", String.class));
        r.setLoggedDate(doc.getString("logged_date"));
        r.setUpdatedDate(doc.getString("updated_date"));
        return r;
    }

    public int getCommunityCount() {
        if (DBConnection.isMongoAvailable()) {
            try {
                return (int) DBConnection.getDatabase().getCollection("users").countDocuments();
            } catch (Exception e) { e.printStackTrace(); }
        }
        return 42; // demo count
    }
}