package com.oracle.ebssdkdemo.demo;

import java.io.IOException;

import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import oracle.apps.fnd.ext.common.AppsRequestWrapper;
import oracle.apps.fnd.ext.common.AppsSessionHelper;
import oracle.apps.fnd.ext.common.Session;

public class Logout extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger =
        Logger.getLogger(Logout.class.getName());

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Logout() {
        super();
    }

    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     * @see HttpServlet#doGet(HttpServletRequest,HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException,
                                                              IOException {
        logger.info("Logout()----logging out the user--");
        //invalidate ICX session & http session
        AppsRequestWrapper wrappedRequest = (AppsRequestWrapper)request;
        Session session = wrappedRequest.getAppsSession();
        //logout only if it is present
        if (session != null) {
            AppsSessionHelper helper =
                new AppsSessionHelper(wrappedRequest.getEbizInstance());
            helper.destroyAppsSession(wrappedRequest.getAppsSession(),
                                      wrappedRequest, response);
        }
        request.getSession(true).invalidate();
        wrappedRequest.getRequestDispatcher("/logout.jsp").forward(wrappedRequest,
                                                                           response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request,HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException,
                                                               IOException {
        doGet(request, response);
    }
}
