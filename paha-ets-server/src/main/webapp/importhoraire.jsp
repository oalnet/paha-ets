<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>Import Horaire</title>
</head>
<body>
<%
    UserService userService = UserServiceFactory.getUserService();
    String thisURL = request.getRequestURI();

    response.setContentType("text/html");
    if (request.getUserPrincipal() != null) {
        response.getWriter().println("<p>Hello, " +
                request.getUserPrincipal().getName() +
                "!  You can <a href=\"" +
                userService.createLogoutURL(thisURL) +
                "\">sign out</a>.</p>");
    } else {
        response.getWriter().println("<p>Please <a href=\"" +
                userService.createLoginURL(thisURL) +
                "\">sign in</a>.</p>");
    }
%>
<form action="/import" method="post">
    <div><textarea name="horaire" rows="5" cols="80"></textarea></div>
    <div><input type="submit" value="Post Horaire"/></div>
</form>
</body>
</html>
