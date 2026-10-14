package com.omnilog.controller;

import java.io.IOException;

import com.omnilog.dao.MediaDAO;
import com.omnilog.dao.UserDAO;
import com.omnilog.model.MediaItem;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class MediaServlet extends HttpServlet {
    private MediaDAO mediaDAO = new MediaDAO();
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "dashboard";

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect("auth?action=login");
            return;
        }

        String userId = (String) session.getAttribute("userId");

        switch (action) {
            case "diary":
                req.setAttribute("diaryList", mediaDAO.getUserDiary(userId));
                req.getRequestDispatcher("diary.jsp").forward(req, resp);
                break;
            case "watchlist":
                req.setAttribute("watchlist", mediaDAO.getWatchlist(userId));
                req.getRequestDispatcher("watchlist.jsp").forward(req, resp);
                break;
            case "profile":
                req.setAttribute("diaryList", mediaDAO.getUserDiary(userId));
                req.setAttribute("watchlist", mediaDAO.getWatchlist(userId));
                req.setAttribute("profileUser", userDAO.findById(userId));
                req.getRequestDispatcher("profile.jsp").forward(req, resp);
                break;
            case "community":
                req.setAttribute("activityList", mediaDAO.getCommunityActivity(50));
                req.setAttribute("communityCount", mediaDAO.getCommunityCount());
                req.getRequestDispatcher("community.jsp").forward(req, resp);
                break;
            case "detail":
                String id = req.getParameter("id");
                MediaItem item = mediaDAO.getMediaById(id);
                if (item == null) { resp.sendError(404, "Media not found"); return; }
                req.setAttribute("media", item);
                req.setAttribute("reviews", mediaDAO.getReviewsForMedia(id));
                req.setAttribute("inWatchlist", mediaDAO.isInWatchlist(userId, id));
                req.getRequestDispatcher("media-detail.jsp").forward(req, resp);
                break;
            case "addMediaForm":
                req.getRequestDispatcher("add-media.jsp").forward(req, resp);
                break;
            case "search":
                String q = req.getParameter("q");
                String type = req.getParameter("type");
                String sort = req.getParameter("sort");
                req.setAttribute("mediaList", mediaDAO.getAllMedia(type, q, sort));
                req.setAttribute("searchQuery", q);
                req.setAttribute("typeFilter", type);
                req.setAttribute("sortBy", sort);
                req.getRequestDispatcher("dashboard.jsp").forward(req, resp);
                break;
            default:
                req.setAttribute("mediaList", mediaDAO.getAllMedia());
                req.getRequestDispatcher("dashboard.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect("auth?action=login");
            return;
        }
        String userId = (String) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String avatar = (String) session.getAttribute("avatar");

        switch (action == null ? "" : action) {
            case "log": {
                String itemId = req.getParameter("itemId");
                int rating = parseRating(req.getParameter("rating"));
                String review = req.getParameter("review");
                String status = req.getParameter("status");
                boolean spoilers = "on".equals(req.getParameter("spoilers"));
                boolean rewatch = "on".equals(req.getParameter("rewatch"));
                if (itemId != null && !itemId.isBlank() && rating >= 1 && rating <= 5) {
                    mediaDAO.logItem(userId, username, avatar, itemId, rating,
                        review == null ? "" : review.trim(), status, spoilers, rewatch);
                }
                resp.sendRedirect("media?action=detail&id=" + itemId);
                break;
            }
            case "updateLog": {
                String logId = req.getParameter("logId");
                int rating = parseRating(req.getParameter("rating"));
                String review = req.getParameter("review");
                String status = req.getParameter("status");
                boolean spoilers = "on".equals(req.getParameter("spoilers"));
                if (logId != null && rating >= 1 && rating <= 5) {
                    mediaDAO.updateLog(userId, logId, rating,
                        review == null ? "" : review.trim(), status, spoilers);
                }
                resp.sendRedirect("media?action=diary");
                break;
            }
            case "deleteLog": {
                String logId = req.getParameter("logId");
                if (logId != null && !logId.isBlank()) mediaDAO.deleteLog(userId, logId);
                resp.sendRedirect("media?action=diary");
                break;
            }
            case "addToWatchlist": {
                String itemId = req.getParameter("itemId");
                if (itemId != null && !itemId.isBlank()) mediaDAO.addToWatchlist(userId, itemId);
                String redirect = req.getParameter("redirect");
                resp.sendRedirect(redirect != null ? redirect : "media?action=watchlist");
                break;
            }
            case "removeFromWatchlist": {
                String itemId = req.getParameter("itemId");
                if (itemId != null && !itemId.isBlank()) mediaDAO.removeFromWatchlist(userId, itemId);
                String redirect = req.getParameter("redirect");
                resp.sendRedirect(redirect != null ? redirect : "media?action=watchlist");
                break;
            }
            case "addMedia": {
                String title = req.getParameter("title");
                String creator = req.getParameter("creator");
                String mediaType = req.getParameter("mediaType");
                String topic = req.getParameter("topic");
                String coverUrl = req.getParameter("coverUrl");
                String description = req.getParameter("description");
                int year = 0;
                try { year = Integer.parseInt(req.getParameter("year")); } catch (Exception ignored) {}
                if (title != null && !title.isBlank()) {
                    mediaDAO.addCustomMedia(title, creator, mediaType, topic, coverUrl,
                        year, description, userId);
                }
                resp.sendRedirect("media?action=dashboard");
                break;
            }
            case "like": {
                String reviewId = req.getParameter("reviewId");
                if (reviewId != null) mediaDAO.toggleLike(userId, reviewId);
                String redirect = req.getParameter("redirect");
                resp.sendRedirect(redirect != null ? redirect : "media?action=community");
                break;
            }
            default:
                resp.sendRedirect("media?action=dashboard");
        }
    }

    private int parseRating(String value) {
        try { return Integer.parseInt(value); } catch (Exception ignored) { return 0; }
    }
}