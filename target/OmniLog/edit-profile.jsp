<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String username = (String) session.getAttribute("username");
    String displayName = (String) session.getAttribute("userName");
    String avatar = (String) session.getAttribute("avatar");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Edit Profile · OmniLog</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<jsp:include page="partials/nav.jsp"/>
<main class="container narrow">
    <h1>Edit Profile</h1>
    <form method="post" action="auth" class="panel log-form">
        <input type="hidden" name="action" value="updateProfile">
        
        <label>Username (cannot be changed)</label>
        <input type="text" value="<%= username %>" disabled style="opacity: 0.5;">
        
        <label>Display Name</label>
        <input type="text" name="displayName" value="<%= displayName != null ? displayName : "" %>" required>
        
        <label>Avatar URL</label>
        <input type="url" name="avatarUrl" value="<%= avatar != null ? avatar : "" %>" placeholder="https://...">
        
        <label>Bio</label>
        <textarea name="bio" rows="3" placeholder="Tell us about yourself..."></textarea>
        
        <button class="btn primary" style="margin-top: 20px;">Save Changes</button>
        <a href="media?action=profile" class="btn">Cancel</a>
    </form>
</main>
</body>
</html>