package org.netflixpp.config;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class JettyConfig {

    public static Server createServer(int port) {
        Server server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Configure Jersey for API routes
        setupJersey(context);

        // Configure static file serving with Jetty's DefaultServlet
        setupStaticFiles(context);

        return server;
    }

    private static void setupJersey(ServletContextHandler context) {
        // Jersey servlet for API routes
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer());
        jerseyServlet.setInitOrder(0);
        // Use our ResourceConfig subclass
        jerseyServlet.setInitParameter("javax.ws.rs.Application",
                "org.netflixpp.config.JerseyConfig");
        context.addServlet(jerseyServlet, "/api/*");
    }

    private static void setupStaticFiles(ServletContextHandler context) {
        // Serve static files from /storage path using Jetty's DefaultServlet
        ServletHolder staticServlet = new ServletHolder("static", DefaultServlet.class);
        staticServlet.setInitParameter("resourceBase", "./storage");
        staticServlet.setInitParameter("pathInfoOnly", "true");
        staticServlet.setInitParameter("dirAllowed", "false");
        context.addServlet(staticServlet, "/storage/*");
    }
}