package org.geektimes.projects.user.web.listener;

import org.geektimes.projects.user.sql.DBConnectionManager;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@WebListener
public class DBConnectionInitializerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Context initializing");
        try {
            String databaseURL = "jdbc:derby:/db/user-platform;create=true";
            Connection connection = DriverManager.getConnection(databaseURL);
            DBConnectionManager.getInstance().setConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnectionByJndi() throws SQLException, NamingException {
        Context initContext = new InitialContext();
        DataSource dataSource = (DataSource) initContext.lookup("java:comp/env/jdbc/UserPlatformDB");
        System.out.println("data source: " + dataSource);
        return dataSource.getConnection();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Context destroying");
        DBConnectionManager.getInstance().releaseConnection();
    }
}
