package com.oracle.ebssdkdemo.demo;

import java.io.IOException;

import java.sql.SQLException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oracle.apps.fnd.ext.common.AppsRequestWrapper;
import oracle.apps.fnd.ext.common.AppsRequestWrapper.WrapperException;

public class EBSWrapperFilter implements Filter {
    private static final Logger logger =
        Logger.getLogger(EBSWrapperFilter.class.getName());

    public void destroy() {
        logger.info("Filter destroyed ");
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException,
                                                   ServletException {
        logger.info("-current URI =" +
                    ((HttpServletRequest)request).getRequestURI());
        AppsRequestWrapper wrapper = null;
        try {
            wrapper =
                    new AppsRequestWrapper((HttpServletRequest)request, (HttpServletResponse)response,
                                           ConnectionProvider.getConnection(),
                                           EBizUtil.getEBizInstance());
        } catch (WrapperException e2) {
            logger.log(Level.SEVERE, "WrapperException error encountered ",
                       e2);
            throw new ServletException(e2);
        } catch (SQLException e2) {
            logger.log(Level.SEVERE, "SQLException error encountered ", e2);
            throw new ServletException(e2);
        }
        try {
            logger.info("Created AppsRequestWrapper object." +
                        " Continuing the filter chain.");
            chain.doFilter(wrapper, response);
            logger.info("- the filter chain ends");
        } finally {
            //AppsRequestWrapper caches a connection internally.
            //AppsRequestWrapper.getConnection()--returns this  connection
            //this connection can be used in doGet()/doPost() service      layer;
            //whenever our application requires a connection in order to
            //service the current request.
            //When AppsRequestWrapper instance is in use, this   connection
            //should not be closed by other code.
            //At this point, we are done using AppsRequestWrapper     instance;
            //so, as good practice, we are going to close (release) this
            //connection now
            if (wrapper != null) {
                logger.info("- releasing the connection attached to the" +
                            " current AppsRequestWrapper instance ");
                try {
                    wrapper.getConnection().close();
                } catch (SQLException e3) {
                    logger.log(Level.WARNING,
                               "SQLException error while closing connection--",
                               e3);

                }
            }
            wrapper = null;
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Filter initialized ");
    }
}
