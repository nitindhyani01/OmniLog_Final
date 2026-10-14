<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Add Media · OmniLog</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<jsp:include page="partials/nav.jsp"/>
<main class="container narrow">
    <h1>Add Media</h1>
    <form method="post" action="media" class="panel">
        <input type="hidden" name="action" value="addMedia">
        <label>Title</label>
        <input type="text" name="title" required>
        <label>Creator (author / director)</label>
        <input type="text" name="creator">
        <label>Type</label>
        <select name="mediaType">
            <option value="BOOK">Book</option>
            <option value="MOVIE">Movie</option>
            <option value="TV_SHOW">TV Show</option>
            <option value="RESEARCH_PAPER">Research Paper</option>
            <option value="ARTICLE">Article</option>
            <option value="GAME">Game</option>
        </select>
        <label>Genre / Topic</label>
        <input type="text" name="topic">
        <label>Release Year</label>
        <input type="number" name="year" min="0" max="2100">
        <label>Cover URL</label>
        <input type="url" name="coverUrl" placeholder="https://…">
        <label>Description</label>
        <textarea name="description" rows="3"></textarea>
        <button class="btn primary">Add Media</button>
    </form>
</main>
</body>
</html>