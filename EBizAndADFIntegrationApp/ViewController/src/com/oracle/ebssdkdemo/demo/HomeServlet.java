package com.oracle.ebssdkdemo.demo;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import oracle.apps.fnd.ext.common.AppsRequestWrapper;
import oracle.apps.fnd.ext.common.CookieStatus;
import oracle.apps.fnd.ext.common.Session;

/**
 Servlet implementation class HomeServlet
 **/

public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger =
        Logger.getLogger(HomeServlet.class.getName());
    private static final String SQL =
        "select CREATION_DATE,LAST_LOGON_DATE from FND_USER " +
        "where USER_ID =:1";

    /**
     * @see HttpServlet#HttpServlet()
     */
    public HomeServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request,HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException,
                                                              IOException {
        AppsRequestWrapper wrappedRequest = (AppsRequestWrapper)request;
        if (!isAuthenticated(wrappedRequest, response)) {
            logger.info("doGet()--No valid FND user ICX session exists" +
                        " currently. Redirecting to EBS AppsLogin page");
            String agent =
                wrappedRequest.getEbizInstance().getAppsServletAgent();
            response.sendRedirect(agent + "AppsLocalLogin.jsp");
            return;
        }
        logger.info("doGet()---we got a valid ICX session.  Proceeding");
        manageAttributes(wrappedRequest, response);
        wrappedRequest.getRequestDispatcher("/home.jsp").forward(wrappedRequest,
                                                                         response);
    }

    private boolean isAuthenticated(AppsRequestWrapper wrappedRequest, HttpServletResponse response) throws ServletException {
        
        Session session = wrappedRequest.getAppsSession(true);
        // It is always good to check for nullability
        // A null value means something went wrong in the JDBC operation
        if (session == null)
            throw new ServletException("Could not initailize ICX session object");
      
        CookieStatus icxCookieStatus =
            session.getCurrentState().getIcxCookieStatus();
        if (!icxCookieStatus.equals(CookieStatus.VALID)) {
            logger.info("Icx session either has expired or is invalid");
            return false;
        }
        return true;
    }
    // we query now --
    // user name --from ICX_SESSIONS table
    // the last time when the user logged into EBS -- from FND_USER  table
    // when the user account was created -- from FND_USER table
    // we intend to display these values on the home page
    

  private void manageAttributes(AppsRequestWrapper wrappedRequest, HttpServletResponse response) throws ServletException {
        // First check whether we already have the values cached in the
        // HTTP session. Note, this is the HTTP session provided by the
        // application server, not our ICX session
        HttpSession httpSession = wrappedRequest.getSession(true);
        String currentUser = (String)httpSession.getAttribute("currentUser");
        Session session = wrappedRequest.getAppsSession(); // this gives us a
        // handle to our ICX
        // session
        //===========================================START=================================================================
        try {
        Map sessionMap=session.getInfo();
        String respId = (String)sessionMap.get("RESPONSIBILITY_ID");
        httpSession.setAttribute("respId", respId);
        PreparedStatement stmt1 = null;
        String resp_name=null;
       
            stmt1 = wrappedRequest.getConnection().prepareStatement("SELECT RESPONSIBILITY_NAME FROM FND_RESPONSIBILITY_TL WHERE RESPONSIBILITY_ID=:1");
            stmt1.setInt(1, Integer.parseInt((String)sessionMap.get("RESPONSIBILITY_ID")));
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
           resp_name = (String)rs1.getObject("RESPONSIBILITY_NAME");
                break;
            }
            httpSession.setAttribute("resp_name", resp_name);
 
        } catch (SQLException e) {
            e.getMessage();
        }
       
        //============================================END================================================================
        if (currentUser == null) { // we need to read it from ICX session
            currentUser = session.getUserName();
            // for performance reason, we can cache it in httpSession
            // for future HTTP requests
            httpSession.setAttribute("currentUser", currentUser);
        }
        Timestamp lastLogon =
            (Timestamp)httpSession.getAttribute("LAST_LOGON");
        Timestamp createdOn =
            (Timestamp)httpSession.getAttribute("CREATED_ON");
        if (lastLogon == null || createdOn == null) { // we are going query EBS
            // DB
            PreparedStatement stmt = null;
            try {
                stmt = wrappedRequest.getConnection().prepareStatement(SQL);
                stmt.setInt(1, Integer.parseInt(session.getUserId()));
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    createdOn = (Timestamp)rs.getObject("CREATION_DATE");
                    lastLogon = (Timestamp)rs.getObject("LAST_LOGON_DATE");
                    logger.info("DB query returned CREATION_DATE=" +
                                createdOn + " ; LAST_LOGON_DATE =" +
                                lastLogon);
                    break; // we are expecting only one row
                }
                //cache values for future
                httpSession.setAttribute("LAST_LOGON", lastLogon);
                httpSession.setAttribute("CREATED_ON", createdOn);
                // we want to display createdOn & lastLogon in a nice human -
                    // readable format
                    
              long  longTime = createdOn.getTime();
                Date date = new Date(longTime);
                // we now format the date as per DATE_FORMAT_MASK for      this;
                // ICX session
                String formattedDate = session.getDateFormat().format(date);
                httpSession.setAttribute("FORMATTED_CREATION_DATE",
                                         formattedDate);
                // Now we process lastLogon
                longTime = lastLogon.getTime();
                date = new Date(longTime);
                String formattedLogonDate =
                    session.getDateFormat().format(date);
                httpSession.setAttribute("FORMATTED_LOGON_DATE",
                                         formattedLogonDate);
                // also process the time (hour:min:second) part
                // Unfortunately session.getDateFormat() (which is based  on
                // underlying DATE_FORMAT_MASK) doesn't have a formatter
                // for the time part, so we are going to do ourselves  using;
                // user Locale
                Calendar cal = Calendar.getInstance(session.getLocale());
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int min = cal.get(Calendar.MINUTE);
                int sec = cal.get(Calendar.SECOND);

                String formattedTime =
                    new StringBuilder().append(hour).append(":").append(min).append(":").append(sec).toString();
                httpSession.setAttribute("FORMATTED_LOGON_TIME",
                                         formattedTime);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, " SQLException error-->", e);
                throw new ServletException(e);
            } catch (Exception e) {
                logger.log(Level.SEVERE, " Unknown error-->", e);
                throw new ServletException(e);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                       System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException,
                                                               IOException {
        AppsRequestWrapper wrappedRequest = (AppsRequestWrapper)request;

        if (!isAuthenticated(wrappedRequest, response)) {
            logger.info("doPost()--No valid FND user ICX session exists" +
                        " currently. Redirecting to EBS AppsLogin page");
           
            String agent =
                wrappedRequest.getEbizInstance().getAppsServletAgent();
            response.sendRedirect(agent + "AppsLocalLogin.jsp");
            return;
        }
        // process the request
        String mesgName = request.getParameter("message_name");
        String appName = request.getParameter("app_name");
        request.setAttribute("message_text",
                             wrappedRequest.getEbizInstance().getMessageDirectory().getMessageText(appName.toUpperCase(),
                                                                                                   mesgName.toUpperCase(),
                                                                                                   wrappedRequest.getConnection()));
        // Note, we are not closing wrappedRequest.getConnection() connection
        // here.
        // Our EBSWrapperFilter takes care of that
        manageAttributes  (wrappedRequest, response);
        
        wrappedRequest.getRequestDispatcher("/home.jsp").forward(wrappedRequest,
                                                                         response);
    }
}
