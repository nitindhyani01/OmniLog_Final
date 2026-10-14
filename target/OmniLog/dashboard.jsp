<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, com.omnilog.model.MediaItem" %>
<%
    List<MediaItem> mediaList = (List<MediaItem>) request.getAttribute("mediaList");
    String searchQuery = (String) request.getAttribute("searchQuery");
    String typeFilter = (String) request.getAttribute("typeFilter");
    String userName = (String) session.getAttribute("userName");
    if (mediaList == null) mediaList = java.util.Collections.emptyList();
%>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard · OmniLog</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<jsp:include page="partials/nav.jsp"/>
<main class="container">
    <h1>Welcome, <%= userName == null ? "friend" : userName %></h1>

    <form method="get" action="media" class="toolbar">
        <input type="hidden" name="action" value="search">
        <input type="text" name="q" placeholder="Search titles or creators…"
               value="<%= searchQuery == null ? "" : searchQuery %>">
        <select name="type">
            <option value="">All types</option>
            <option value="BOOK" <%= "BOOK".equals(typeFilter) ? "selected" : "" %>>Books</option>
            <option value="MOVIE" <%= "MOVIE".equals(typeFilter) ? "selected" : "" %>>Movies</option>
            <option value="TV_SHOW" <%= "TV_SHOW".equals(typeFilter) ? "selected" : "" %>>TV</option>
            <option value="RESEARCH_PAPER" <%= "RESEARCH_PAPER".equals(typeFilter) ? "selected" : "" %>>Papers</option>
            <option value="ARTICLE" <%= "ARTICLE".equals(typeFilter) ? "selected" : "" %>>Articles</option>
            <option value="GAME" <%= "GAME".equals(typeFilter) ? "selected" : "" %>>Games</option>
        </select>
        <select name="sort">
            <option value="">Newest</option>
            <option value="rating" <%= "rating".equals(request.getAttribute("sortBy")) ? "selected" : "" %>>Top Rated</option>
            <option value="title" <%= "title".equals(request.getAttribute("sortBy")) ? "selected" : "" %>>Title A–Z</option>
        </select>
        <button class="btn">Search</button>
        <a class="btn primary" href="media?action=addMediaForm">+ Add Media</a>
    </form>

    <div class="grid">
        <% if (mediaList.isEmpty()) { %>
            <p class="empty">No items found.</p>
        <% } else {
             for (MediaItem m : mediaList) {
                 if (m == null) continue;
                 String cover = m.getCoverUrl();
                 if (cover == null || cover.isBlank()) cover = "https://via.placeholder.com/300x450?text=No+Cover";
                 String title = m.getTitle() == null ? "(untitled)" : m.getTitle();
                 String creator = m.getCreator() == null ? "" : m.getCreator();
                 String type = m.getMediaType() == null ? "UNKNOWN" : m.getMediaType();
                 String id = m.getId() == null ? "" : m.getId();
        %>
            <a class="card" href="media?action=detail&id=<%= id %>">
                <img src="<%= cover %>" alt="<%= title %>" loading="lazy">
                <div class="card-body">
                    <span class="badge"><%= type %></span>
                    <h3><%= title %></h3>
                    <p class="creator"><%= creator %></p>
                    <p class="meta">★ <%= m.getAverageRating() %> · <%= m.getRatingCount() %> ratings</p>
                </div>
            </a>
        <%   }
           } %>
    </div>
</main>
</body>
</html>