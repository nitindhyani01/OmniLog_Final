package com.omnilog.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnilog.dao.MediaDAO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DataFetcher {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final MediaDAO dao;

    public DataFetcher(MediaDAO dao) {
        this.dao = dao;
    }

    public int fetchAll() {
        return fetchBooks() + fetchPapers();
    }

    // ---------- BOOKS (Open Library, no key) ----------
    private int fetchBooks() {
        int inserted = 0;

        String[] subjects = {
            "science_fiction", "fantasy", "mystery", "romance",
            "history", "philosophy", "computer_science", "psychology",
            "biography", "business", "poetry", "thriller",
            "adventure", "horror", "drama", "music",
            "art", "science", "mathematics", "economics"
        };

        for (String subject : subjects) {
            try {
                String url = "https://openlibrary.org/search.json?subject="
                        + subject
                        + "&limit=20&fields=title,author_name,first_publish_year,cover_i";
                String body = get(url);
                if (body == null) continue;

                JsonNode docs = JSON.readTree(body).get("docs");
                if (docs == null) continue;

                for (JsonNode doc : docs) {
                    String title = doc.path("title").asText("");
                    if (title.isBlank()) continue;

                    String author = "";
                    JsonNode authors = doc.get("author_name");
                    if (authors != null && authors.size() > 0)
                        author = authors.get(0).asText("");

                    int year = doc.path("first_publish_year").asInt(0);

                    String cover;
                    int coverId = doc.path("cover_i").asInt(0);
                    cover = (coverId > 0)
                            ? "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg"
                            : placeholder(title);

                    dao.addCustomMedia(title, author, "BOOK",
                            subject.replace("_", " "),
                            cover, year, "", "system");
                    inserted++;
                }
            } catch (Exception e) {
                System.out.println("[DataFetcher] OpenLibrary error (" + subject + "): " + e.getMessage());
            }
        }
        System.out.println("[DataFetcher] Inserted " + inserted + " books.");
        return inserted;
    }

    // ---------- PAPERS / ARTICLES (curated) ----------
    private int fetchPapers() {
        String[][] papers = {
            {"Attention Is All You Need", "Vaswani et al.", "RESEARCH_PAPER", "Deep Learning", "2017"},
            {"ImageNet Classification with Deep CNNs", "Krizhevsky et al.", "RESEARCH_PAPER", "Computer Vision", "2012"},
            {"BERT", "Devlin et al.", "RESEARCH_PAPER", "NLP", "2018"},
            {"GPT-3: Language Models are Few-Shot Learners", "Brown et al.", "RESEARCH_PAPER", "NLP", "2020"},
            {"Deep Residual Learning for Image Recognition", "He et al.", "RESEARCH_PAPER", "Computer Vision", "2015"},
            {"Generative Adversarial Networks", "Goodfellow et al.", "RESEARCH_PAPER", "Deep Learning", "2014"},
            {"A Mathematical Theory of Communication", "Claude Shannon", "RESEARCH_PAPER", "Information Theory", "1948"},
            {"On Computable Numbers", "Alan Turing", "RESEARCH_PAPER", "Theory of Computation", "1936"},
            {"The PageRank Citation Ranking", "Page and Brin", "RESEARCH_PAPER", "Information Retrieval", "1998"},
            {"MapReduce: Simplified Data Processing", "Dean and Ghemawat", "RESEARCH_PAPER", "Distributed Systems", "2004"},
            {"Dynamo: Amazon's Highly Available Key-Value Store", "DeCandia et al.", "RESEARCH_PAPER", "Distributed Systems", "2007"},
            {"Bitcoin: A Peer-to-Peer Electronic Cash System", "Satoshi Nakamoto", "RESEARCH_PAPER", "Cryptography", "2008"},
            {"AlphaGo", "Silver et al.", "RESEARCH_PAPER", "Reinforcement Learning", "2016"},
            {"Deep Learning", "LeCun, Bengio, Hinton", "RESEARCH_PAPER", "Deep Learning", "2015"},
            {"The Unreasonable Effectiveness of Data", "Halevy et al.", "RESEARCH_PAPER", "Machine Learning", "2009"},
            {"Reflections on Trusting Trust", "Ken Thompson", "ARTICLE", "Cybersecurity", "1984"},
            {"You and Your Research", "Richard Hamming", "ARTICLE", "Career", "1986"},
            {"The Cathedral and the Bazaar", "Eric S. Raymond", "ARTICLE", "Open Source", "1997"},
            {"Worse Is Better", "Richard Gabriel", "ARTICLE", "Software Design", "1989"},
            {"No Silver Bullet", "Fred Brooks", "ARTICLE", "Software Engineering", "1986"},
            {"The Law of Leaky Abstractions", "Joel Spolsky", "ARTICLE", "Software Engineering", "2002"},
            {"Big Ball of Mud", "Foote and Yoder", "ARTICLE", "Software Architecture", "1997"},
            {"Programming as Theory Building", "Peter Naur", "ARTICLE", "Software Engineering", "1985"},
            {"The Bitter Lesson", "Rich Sutton", "ARTICLE", "AI", "2019"}
        };

        int inserted = 0;
        for (String[] p : papers) {
            dao.addCustomMedia(p[0], p[1], p[2], p[3],
                    placeholder(p[0]), Integer.parseInt(p[4]), "", "system");
            inserted++;
        }
        System.out.println("[DataFetcher] Inserted " + inserted + " papers/articles.");
        return inserted;
    }

    // ---------- helpers ----------
    private String get(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(12))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) return resp.body();
            System.out.println("[DataFetcher] HTTP " + resp.statusCode() + " for " + url);
        } catch (Exception e) {
            System.out.println("[DataFetcher] Request failed: " + e.getMessage());
        }
        return null;
    }

    private static String placeholder(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return "https://picsum.photos/seed/" + slug + "/300/450";
    }
}