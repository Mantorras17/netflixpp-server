package org.netflixpp.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.netflixpp.filter.CORSFilter;
import org.netflixpp.filter.JWTFilter;
import org.netflixpp.filter.LoggingFilter;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        // Filters (ORDER MATTERS!)
        register(LoggingFilter.class);    // First - log all requests
        register(JWTFilter.class);        // Second - authentication
        register(CORSFilter.class);       // Third - CORS headers

        // JSON support
        register(org.glassfish.jersey.jackson.JacksonFeature.class);

        // Scan backend controllers (they will be in classpath)
        packages("org.netflixpp.controller");

        // Multipart support for file uploads
        register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
    }
}