<html>
<head>
    <title>Send a message</title>
</head>

<body>
    <form action="<%= request.getContextPath() %>/" method="post">
        <input name="text">
        <input type="submit">
    </form>
    ${message}
</body>
</html>