<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Register · OmniLog</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body class="auth-page">
<div class="auth-card">
    <h1>Join OmniLog</h1>
    <p class="subtitle">Start tracking what you consume.</p>
    <% String error = (String) request.getAttribute("error");
       if (error != null) { %>
        <div class="alert error"><%= error %></div>
    <% } %>
    <form method="post" action="auth">
        <input type="hidden" name="action" value="register">
        <label>Username</label>
        <input type="text" name="username" required>
        <label>Email</label>
        <input type="email" name="email" required>
        <label>Display Name</label>
        <input type="text" name="displayName" placeholder="Optional">
        <label>Password</label>
        <input type="password" name="password" minlength="6" required>
        <label>Confirm Password</label>
        <input type="password" name="confirmPassword" minlength="6" required>
        <button type="submit" class="btn primary full">Create Account</button>
    </form>
    <p class="center">Already have an account? <a href="auth?action=login">Log in</a></p>
</div>
</body>
</html>