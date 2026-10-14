package com.omnilog.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.omnilog.dao.MediaDAO;
import com.omnilog.model.MediaItem;
import com.omnilog.model.Review;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiServlet extends HttpServlet {

    private final MediaDAO mediaDAO = new MediaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String path = req.getPathInfo();
        PrintWriter out = resp.getWriter();

        if (path == null || "/media".equals(path)) {
            List<MediaItem> list = mediaDAO.getAllMedia(
                req.getParameter("type"),
                req.getParameter("q"),
                req.getParameter("sort"));
            out.print(toJsonArray(list));

        } else if (path.startsWith("/media/")) {
            String id = path.substring("/media/".length());
            MediaItem m = mediaDAO.getMediaById(id);
            if (m == null) {
                resp.setStatus(404);
                out.print("{\"error\":\"not found\"}");
            } else {
                out.print(toJson(m));
            }

        } else if (path.startsWith("/reviews/")) {
            String mediaId = path.substring("/reviews/".length());
            out.print(reviewsToJson(mediaDAO.getReviewsForMedia(mediaId)));

        } else if ("/stats".equals(path)) {
            int mediaCount = mediaDAO.getAllMedia().size();
            out.print("{\"media\":" + mediaCount + ",\"health\":\"ok\"}");

        } else if ("/health".equals(path)) {
            out.print("{\"status\":\"ok\"}");

        } else {
            resp.setStatus(404);
            out.print("{\"error\":\"unknown endpoint\"}");
        }

        out.flush();
    }

    // ------------------------------------------------------------
    // JSON serializers
    // ------------------------------------------------------------

    private String toJsonArray(List<MediaItem> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(list.get(i)));
        }
        return sb.append("]").toString();
    }

    private String toJson(MediaItem m) {
        return "{"
            + "\"id\":"          + q(m.getId())           + ","
            + "\"title\":"       + q(m.getTitle())        + ","
            + "\"creator\":"     + q(m.getCreator())      + ","
            + "\"type\":"        + q(m.getMediaType())    + ","
            + "\"topic\":"       + q(m.getGenreOrTopic()) + ","
            + "\"cover\":"       + q(m.getCoverUrl())     + ","
            + "\"year\":"        + m.getReleaseYear()     + ","
            + "\"rating\":"      + m.getAverageRating()   + ","
            + "\"ratingCount\":" + m.getRatingCount()     + ","
            + "\"reviewCount\":" + m.getReviewCount()
            + "}";
    }

    private String reviewsToJson(List<Review> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            Review r = list.get(i);
            sb.append("{")
              .append("\"id\":").append(q(r.getId())).append(",")
              .append("\"username\":").append(q(r.getUsername())).append(",")
              .append("\"rating\":").append(r.getRating()).append(",")
              .append("\"review\":").append(q(r.getReviewText())).append(",")
              .append("\"status\":").append(q(r.getStatus())).append(",")
              .append("\"date\":").append(q(r.getLoggedDate())).append(",")
              .append("\"likes\":").append(r.getLikeCount())
              .append("}");
        }
        return sb.append("]").toString();
    }

    private String q(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}