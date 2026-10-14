<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, com.omnilog.model.Review, com.omnilog.model.MediaItem, com.omnilog.model.User" %>
<%
    List<Review> diaryList    = (List<Review>)    request.getAttribute("diaryList");
    List<MediaItem> watchlist = (List<MediaItem>) request.getAttribute("watchlist");
    User profileUser          = (User)            request.getAttribute("profileUser");
    if (diaryList == null) diaryList = java.util.Collections.emptyList();
    if (watchlist == null) watchlist = java.util.Collections.emptyList();
%>
<!DOCTYPE html>
<html>
<head>
    <title>Profile · OmniLog</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<jsp:include page="partials/nav.jsp"/>
<main class="container">
    <% if (profileUser == null) { %>
        <p class="empty">User not found. <a href="media">Back to browse</a></p>
    <% } else { %>
    <div class="profile-header">
        <img src="<%= profileUser.getAvatarUrl() == null ? "" : profileUser.getAvatarUrl() %>"
             class="avatar-large" alt="">
        <div>
            <h1><%= profileUser.getDisplayName() == null ? profileUser.getUsername() : profileUser.getDisplayName() %></h1>
            <p class="meta">@<%= profileUser.getUsername() %> · joined <%= profileUser.getJoinedDate() %></p>
            <p><%= profileUser.getBio() == null ? "" : profileUser.getBio() %></p>
            <p class="stats">
                <strong><%= diaryList.size() %></strong> logged ·
                <strong><%= watchlist.size() %></strong> watchlist
            </p>
        </div>
    </div>

    <section class="panel">
        <h2>Recent Activity</h2>
        <% if (diaryList.isEmpty()) { %>
            <p class="empty">No activity yet.</p>
        <% } else { for (int i = 0; i < Math.min(diaryList.size(), 5); i++) {
            Review r = diaryList.get(i); %>
            <div class="review">
                <img src="<%= r.getMediaCover() == null ? "" : r.getMediaCover() %>" class="thumb" alt="">
                <div>
                    <strong><%= r.getMediaTitle() %></strong>
                    <span class="meta"> ★ <%= r.getRating() %> · <%= r.getLoggedDate() %></span>
                    <p><%= r.getReviewText() == null ? "" : r.getReviewText() %></p>
                </div>
            </div>
        <% } } %>
    </section>
    <% } %>
</main>
</body>
</html>