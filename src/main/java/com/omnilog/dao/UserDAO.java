package com.omnilog.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Filters.regex;
import com.omnilog.model.User;
import com.omnilog.util.DBConnection;
import com.omnilog.util.PasswordUtil;

public class UserDAO {
    // Fallback in-memory user store
    private static final Map<String, User> fallbackUsers = new ConcurrentHashMap<>();
    private static final Map<String, String> fallbackSalts = new ConcurrentHashMap<>();

    static {
        // Seed demo user
        User demo = new User("nitin", "nitin@omnilog.com",
            PasswordUtil.hashPassword("password123", "seedSalt12345678"), "Nitin Dhyani");
        demo.setId("user_1");
        demo.setBio("Software engineer & lifelong learner.");
        fallbackUsers.put("user_1", demo);
        fallbackSalts.put("user_1", "seedSalt12345678");
    }

    public User authenticate(String username, String password) {
        if (DBConnection.isMongoAvailable()) {
            try {
                MongoCollection<Document> col = DBConnection.getDatabase().getCollection("users");
                Document doc = col.find(or(eq("username", username), eq("email", username))).first();
                if (doc != null) {
                    String salt = doc.getString("salt");
                    String hash = doc.getString("password_hash");
                    if (PasswordUtil.verifyPassword(password, salt, hash)) {
                        return docToUser(doc);
                    }
                }
                return null;
            } catch (Exception e) { e.printStackTrace(); }
        }
        // Fallback
        for (User u : fallbackUsers.values()) {
            if (u.getUsername().equals(username) || u.getEmail().equals(username)) {
                String salt = fallbackSalts.get(u.getId());
                if (PasswordUtil.verifyPassword(password, salt, u.getPasswordHash())) {
                    return u;
                }
            }
        }
        return null;
    }

    public boolean register(User user, String plainPassword) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(plainPassword, salt);
        user.setPasswordHash(hash);

        if (DBConnection.isMongoAvailable()) {
            try {
                MongoCollection<Document> col = DBConnection.getDatabase().getCollection("users");
                if (col.find(or(eq("username", user.getUsername()), eq("email", user.getEmail()))).first() != null) {
                    return false;
                }
                Document doc = new Document("username", user.getUsername())
                    .append("email", user.getEmail())
                    .append("password_hash", hash)
                    .append("salt", salt)
                    .append("display_name", user.getDisplayName())
                    .append("avatar_url", user.getAvatarUrl())
                    .append("bio", user.getBio() == null ? "" : user.getBio())
                    .append("joined_date", user.getJoinedDate())
                    .append("follower_count", 0)
                    .append("following_count", 0);
                col.insertOne(doc);
                user.setId(doc.getObjectId("_id").toHexString());
                return true;
            } catch (Exception e) { e.printStackTrace(); return false; }
        }

        // Fallback
        for (User u : fallbackUsers.values()) {
            if (u.getUsername().equals(user.getUsername()) || u.getEmail().equals(user.getEmail())) {
                return false;
            }
        }
        String id = "user_" + (fallbackUsers.size() + 1);
        user.setId(id);
        fallbackUsers.put(id, user);
        fallbackSalts.put(id, salt);
        return true;
    }

    public User findById(String userId) {
        if (DBConnection.isMongoAvailable()) {
            try {
                MongoCollection<Document> col = DBConnection.getDatabase().getCollection("users");
                Document doc = null;
                try { doc = col.find(eq("_id", new ObjectId(userId))).first(); }
                catch (IllegalArgumentException ignored) {}
                if (doc != null) return docToUser(doc);
            } catch (Exception e) { e.printStackTrace(); }
        }
        return fallbackUsers.get(userId);
    }

    public User findByUsername(String username) {
        if (DBConnection.isMongoAvailable()) {
            try {
                Document doc = DBConnection.getDatabase().getCollection("users")
                    .find(eq("username", username)).first();
                if (doc != null) return docToUser(doc);
            } catch (Exception e) { e.printStackTrace(); }
        }
        for (User u : fallbackUsers.values())
            if (u.getUsername().equals(username)) return u;
        return null;
    }

    public boolean updateProfile(String userId, String displayName, String bio, String avatarUrl) {
        if (DBConnection.isMongoAvailable()) {
            try {
                MongoCollection<Document> col = DBConnection.getDatabase().getCollection("users");
                Document updates = new Document()
                    .append("display_name", displayName)
                    .append("bio", bio)
                    .append("avatar_url", avatarUrl);
                col.updateOne(eq("_id", new ObjectId(userId)), new Document("$set", updates));
                return true;
            } catch (Exception e) { e.printStackTrace(); }
        }
        User u = fallbackUsers.get(userId);
        if (u != null) {
            u.setDisplayName(displayName);
            u.setBio(bio);
            u.setAvatarUrl(avatarUrl);
            return true;
        }
        return false;
    }

    public List<User> searchUsers(String query) {
        List<User> results = new ArrayList<>();
        if (DBConnection.isMongoAvailable()) {
            try {
                MongoCollection<Document> col = DBConnection.getDatabase().getCollection("users");
                for (Document doc : col.find(or(
                        regex("username", query, "i"),
                        regex("display_name", query, "i"))).limit(20)) {
                    results.add(docToUser(doc));
                }
                return results;
            } catch (Exception e) { e.printStackTrace(); }
        }
        for (User u : fallbackUsers.values()) {
            if (u.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                u.getDisplayName().toLowerCase().contains(query.toLowerCase())) {
                results.add(u);
            }
        }
        return results;
    }

    private User docToUser(Document doc) {
        User u = new User();
        u.setId(doc.getObjectId("_id").toHexString());
        u.setUsername(doc.getString("username"));
        u.setEmail(doc.getString("email"));
        u.setPasswordHash(doc.getString("password_hash"));
        u.setDisplayName(doc.getString("display_name"));
        u.setBio(doc.getString("bio"));
        u.setAvatarUrl(doc.getString("avatar_url"));
        u.setJoinedDate(doc.getString("joined_date"));
        u.setFollowerCount(doc.getInteger("follower_count", 0));
        u.setFollowingCount(doc.getInteger("following_count", 0));
        return u;
    }
}