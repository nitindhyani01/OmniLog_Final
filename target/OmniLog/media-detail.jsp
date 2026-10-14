<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, com.omnilog.model.MediaItem, com.omnilog.model.Review" %>
<%
    MediaItem media       = (MediaItem)    request.getAttribute("media");
    List<Review> reviews  = (List<Review>) request.getAttribute("reviews");
    boolean inWatchlist   = Boolean.TRUE.equals(request.getAttribute("inWatchlist"));
    if (reviews == null) reviews = java.util.Collections.emptyList();
%>
<!DOCTYPE html>
<html>
<head>
    <title><%= media.getTitle() %> · OmniLog</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<jsp:include page="partials/nav.jsp"/>
<main class="container">
    <div class="detail-hero">
        <img src="<%= media.getCoverUrl() %>" alt="<%= media.getTitle() %>" class="detail-cover">
        <div class="detail-info">
            <span class="badge"><%= media.getMediaType() %></span>
            <h1><%= media.getTitle() %></h1>
            <p class="creator">by <%= media.getCreator() %> <%= media.getReleaseYear() > 0 ? "· " + media.getReleaseYear() : "" %></p>
            <p class="meta">★ <%= media.getAverageRating() %> · <%= media.getRatingCount() %> ratings · <%= media.getReviewCount() %> reviews</p>
            <p><%= media.getDescription() == null ? "" : media.getDescription() %></p>

            <div class="actions">
                <button class="btn primary" onclick="document.getElementById('logForm').scrollIntoView({behavior:'smooth'})">
                    Log / Review
                </button>
                <form method="post" action="media" style="display:inline">
                    <input type="hidden" name="action" value="<%= inWatchlist ? "removeFromWatchlist" : "addToWatchlist" %>">
                    <input type="hidden" name="itemId" value="<%= media.getId() %>">
                    <input type="hidden" name="redirect" value="media?action=detail&id=<%= media.getId() %>">
                    <button class="btn"><%= inWatchlist ? "Remove from Watchlist" : "+ Watchlist" %></button>
                </form>
            </div>
        </div>
    </div>

    <section id="logForm" class="panel">
        <h2>Log this</h2>
        <form method="post" action="media" class="log-form">
            <input type="hidden" name="action" value="log">
            <input type="hidden" name="itemId" value="<%= media.getId() %>">
            <label>Rating</label>
            <div class="stars">
            <% for (int i = 1; i <= 5; i++) { %>
            <label>
            <input type="radio" name="rating" value="<%= i %>" required>
            <span>★</span>
            </label>
            <% } %>
            </div>
            <label>Status</label>
            <select name="status">
                <option value="WATCHED">Watched</option>
                <option value="WATCHING">Watching</option>
                <option value="WANT_TO_WATCH">Want to watch</option>
                <option value="DROPPED">Dropped</option>
            </select>
            <label>Review (optional)</label>
            <textarea name="review" rows="4" placeholder="What did you think?"></textarea>
            <label class="inline"><input type="checkbox" name="spoilers"> Contains spoilers</label>
            <label class="inline"><input type="checkbox" name="rewatch"> Rewatch / Re-read</label>
            <button class="btn primary">Save</button>
        </form>
    </section>

    <section class="panel">
        <h2>Reviews (<%= reviews == null ? 0 : reviews.size() %>)</h2>
        <% if (reviews == null || reviews.isEmpty()) { %>
            <p class="empty">No reviews yet. Be the first!</p>
        <% } else { for (Review r : reviews) { %>
            <div class="review">
                <img src="<%= r.getUserAvatar() %>" class="avatar" alt="">
                <div class="review-body">
                    <strong><%= r.getUsername() %></strong>
                    <span class="meta"> ★ <%= r.getRating() %> · <%= r.getStatus() %> · <%= r.getLoggedDate() %></span>
                    <% if (r.isContainsSpoilers()) { %><span class="badge warn">Spoilers</span><% } %>
                    <p><%= r.getReviewText() %></p>
                    <form method="post" action="media" style="display:inline">
                        <input type="hidden" name="action" value="like">
                        <input type="hidden" name="reviewId" value="<%= r.getId() %>">
                        <input type="hidden" name="redirect" value="media?action=detail&id=<%= media.getId() %>">
                        <button class="btn tiny">♥ <%= r.getLikeCount() %></button>
                    </form>
                </div>
            </div>
        <% } } %>
    </section>
</main>
</body>
</html>