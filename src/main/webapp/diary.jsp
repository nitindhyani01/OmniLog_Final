<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, com.omnilog.model.Review" %>
<%
    List<Review> diaryList = (List<Review>) request.getAttribute("diaryList");
    if (diaryList == null) diaryList = java.util.Collections.emptyList();
%>
<!DOCTYPE html>
<html>
<head>
    <title>Diary · OmniLog</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<jsp:include page="partials/nav.jsp"/>
<main class="container">
    <h1>My Diary</h1>
    <% if (diaryList.isEmpty()) { %>
        <p class="empty">Your diary is empty. <a href="media">Browse media</a> to start logging.</p>
    <% } else { for (Review r : diaryList) { %>
        <div class="diary-entry">
            <img src="<%= r.getMediaCover() == null ? "" : r.getMediaCover() %>" alt="">
            <div class="diary-body">
                <a href="media?action=detail&id=<%= r.getMediaId() %>"><h3><%= r.getMediaTitle() %></h3></a>
                <p class="meta">★ <%= r.getRating() %> · <%= r.getStatus() %>
                   · <%= r.getLoggedDate() %>
                   <%= r.isRewatch() ? "· Rewatch" : "" %></p>
                <p><%= r.getReviewText() == null ? "" : r.getReviewText() %></p>
                <form method="post" action="media" class="inline-form"
                      onsubmit="return confirm('Delete this entry?')">
                    <input type="hidden" name="action" value="deleteLog">
                    <input type="hidden" name="logId" value="<%= r.getId() %>">
                    <button class="btn tiny danger">Delete</button>
                </form>
            </div>
        </div>
    <% } } %>
</main>
</body>
</html>