<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, com.omnilog.model.MediaItem" %>
<%
    List<MediaItem> watchlist = (List<MediaItem>) request.getAttribute("watchlist");
    if (watchlist == null) watchlist = java.util.Collections.emptyList();
%>
<!DOCTYPE html>
<html>
<head>
    <title>Watchlist · OmniLog</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<jsp:include page="partials/nav.jsp"/>
<main class="container">
    <h1>My Watchlist</h1>
    <% if (watchlist.isEmpty()) { %>
        <p class="empty">Nothing here yet. <a href="media">Browse media</a> and add some.</p>
    <% } else { %>
        <div class="grid">
        <% for (MediaItem m : watchlist) {
             String cover = m.getCoverUrl();
             if (cover == null || cover.isBlank()) cover = "https://via.placeholder.com/300x450?text=No+Cover";
             String title = m.getTitle() == null ? "(untitled)" : m.getTitle();
             String type = m.getMediaType() == null ? "UNKNOWN" : m.getMediaType();
             String creator = m.getCreator() == null ? "" : m.getCreator();
        %>
            <div class="card">
                <a href="media?action=detail&id=<%= m.getId() %>">
                    <img src="<%= cover %>" alt="<%= title %>">
                </a>
                <div class="card-body">
                    <span class="badge"><%= type %></span>
                    <h3><%= title %></h3>
                    <p class="creator"><%= creator %></p>
                    <form method="post" action="media">
                        <input type="hidden" name="action" value="removeFromWatchlist">
                        <input type="hidden" name="itemId" value="<%= m.getId() %>">
                        <button class="btn tiny danger">Remove</button>
                    </form>
                </div>
            </div>
        <% } %>
        </div>
    <% } %>
</main>
</body>
</html>