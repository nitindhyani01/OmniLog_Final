<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String avatar = (String) session.getAttribute("avatar");
    String userName = (String) session.getAttribute("userName");
%>
<nav class="nav">
    <a href="media?action=dashboard" class="brand">OmniLog</a>
    <div class="links">
        <a href="media?action=recommended">For You</a>
        <a href="media?action=dashboard">Browse</a>
        <a href="media?action=diary">Diary</a>
        <a href="media?action=watchlist">Watchlist</a>
        <a href="media?action=community">Community</a>
        <a href="media?action=profile">
            <% if (avatar != null) { %><img src="<%= avatar %>" class="nav-avatar"><% } %>
            <%= userName %>
        </a>
        <a href="auth?action=logout" class="btn tiny">Logout</a>
    </div>
</nav>