<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, com.omnilog.model.Review" %>
<%
    List<Review> activityList = (List<Review>) request.getAttribute("activityList");
    if (activityList == null) activityList = java.util.Collections.emptyList();
    Integer communityCount = (Integer) request.getAttribute("communityCount");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Community · OmniLog</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<jsp:include page="partials/nav.jsp"/>
<main class="container">
    <h1>Community Activity</h1>
    <p class="meta"><%= communityCount == null ? 0 : communityCount %> members</p>
    <% if (activityList.isEmpty()) { %>
        <p class="empty">No activity yet.</p>
    <% } else { for (Review r : activityList) { %>
        <div class="review">
            <img src="<%= r.getUserAvatar() == null ? "" : r.getUserAvatar() %>" class="avatar" alt="">
            <div class="review-body">
                <strong><%= r.getUsername() == null ? "Anonymous" : r.getUsername() %></strong>
                <span class="meta"> rated </span>
                <a href="media?action=detail&id=<%= r.getMediaId() %>"><%= r.getMediaTitle() %></a>
                <span class="meta"> ★ <%= r.getRating() %> · <%= r.getLoggedDate() %></span>
                <% if (r.isContainsSpoilers()) { %><span class="badge warn">Spoilers</span><% } %>
                <p><%= r.getReviewText() == null ? "" : r.getReviewText() %></p>
                <form method="post" action="media" style="display:inline">
                    <input type="hidden" name="action" value="like">
                    <input type="hidden" name="reviewId" value="<%= r.getId() %>">
                    <input type="hidden" name="redirect" value="media?action=community">
                    <button class="btn tiny">♥ <%= r.getLikeCount() %></button>
                </form>
            </div>
        </div>
    <% } } %>
</main>
</body>
</html>