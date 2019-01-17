<%@ page language="java" contentType="text/html; charset=windows-1252"
         pageEncoding="UTF-8"%>
<%@ page import="java.sql.Timestamp"%>
<%@ page import="java.util.Date"%>
<%@ page import="oracle.apps.fnd.ext.common.AppsRequestWrapper"%>
<%@ page import="oracle.apps.fnd.ext.common.EBiz"%>
<%@ page import="oracle.apps.fnd.ext.common.Session"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html;
charset=windows-1252"/>
    <title>Demo Home Page</title>
  </head>
  <body>
    <div>
      <a href="<%=request.getContextPath()%>/logout">Log out</a>
    </div>
    <h4>Your profile</h4>
     
    <%
AppsRequestWrapper wrappedRequest = (AppsRequestWrapper) request;
EBiz ebiz = wrappedRequest.getEbizInstance();
%>
    <div>
      <%=
ebiz.getMessageDirectory().getMessageText("FND","FND_SSO_USER_NAME",
wrappedRequest.getLangCode(),wrappedRequest.getConnection()
) %>
       : 
      <%= (String)session.getAttribute("currentUser") %>
       
      <br/>
       Current Responsibility ID: 
      <%=
(String)session.getAttribute("respId") %>
<br/>
       Current Responsibility Name: 
      <%=
(String)session.getAttribute("resp_name") %>
 <br/>
       Registered since : 
      <%=
(String)session.getAttribute("FORMATTED_CREATION_DATE") %><br/>
       Last login on 
      <%=
(String)session.getAttribute("FORMATTED_LOGON_TIME") %>, 
      <%=
(String)session.getAttribute("FORMATTED_LOGON_DATE") %>
    </div>
    <div>
      Query for a message text here.<br/>
       Note, we are not doing any input validation here. Please be nice and
      provide only valid inputs! 
      <br/>
       
      <form action="<%=request.getContextPath()%>/home" method="post">
        <p>
          Message Name:<input name="message_name" type="text"/><br/>
           (such as FND)
        </p>
        <p>
          App ShortName:<input name="app_name" type="text"/><br/>
           (such as ABOUT_PAGE_ERROR_MSG)<input type="submit"/>
        </p>
      </form>
    </div>
    <%
String method = request.getMethod();
if("POST".equalsIgnoreCase(method) ){
%>
     You queried for message name 
    <%=
request.getParameter("message_name") %>, application shortName 
    <%= request.getParameter("app_name") %>
     
    <br/>
     The result is: 
    <%= (String)request.getAttribute("message_text") %>
     
    <%
}
%>
  </body>
</html>