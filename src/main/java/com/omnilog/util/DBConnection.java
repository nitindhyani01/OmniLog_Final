package com.omnilog.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DBConnection {
    private static final String URI = System.getenv().getOrDefault(
        "MONGODB_URI", "mongodb://localhost:27017");
    private static final String DB_NAME = "omnilog_db";

    private static MongoClient mongoClient = null;
    private static boolean mongoAvailable = false;
    private static boolean tested = false;

    public static synchronized MongoDatabase getDatabase() {
        if (!tested) {
            try {
                mongoClient = MongoClients.create(URI);
                mongoClient.listDatabaseNames().first();
                mongoAvailable = true;
                System.out.println("[OmniLog] Connected to MongoDB at " + URI);
            } catch (Throwable t) {
                mongoAvailable = false;
                System.out.println("[OmniLog] MongoDB unavailable - using in-memory fallback.");
            }
            tested = true;
        }
        if (mongoAvailable && mongoClient != null) {
            try { return mongoClient.getDatabase(DB_NAME); }
            catch (Exception e) { return null; }
        }
        return null;
    }

    public static boolean isMongoAvailable() {
        if (!tested) getDatabase();
        return mongoAvailable;
    }

    public static void close() {
        if (mongoClient != null) {
            try { mongoClient.close(); } catch (Exception ignored) {}
            mongoClient = null;
            tested = false;
        }
    }
}