<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login · OmniLog</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body class="auth-page">
<div class="auth-card">
    <h1>OmniLog</h1>
    <p class="subtitle">Track books, movies, papers, and more.</p>
    <% String error = (String) request.getAttribute("error");
       if (error != null) { %>
        <div class="alert error"><%= error %></div>
    <% } %>
    <form method="post" action="auth">
        <input type="hidden" name="action" value="login">
        <label>Username or Email</label>
        <input type="text" name="username" required autofocus>
        <label>Password</label>
        <input type="password" name="password" required>
        <button type="submit" class="btn primary full">Log In</button>
    </form>
    <p class="hint">Demo: <code>nitin</code> / <code>password123</code></p>
    <p class="center">New here? <a href="auth?action=register">Create an account</a></p>
</div>
</body>
</html>