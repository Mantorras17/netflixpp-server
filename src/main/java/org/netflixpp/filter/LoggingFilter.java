package org.netflixpp.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = LogManager.getLogger(LoggingFilter.class);

    @Context
    private UriInfo uriInfo;

    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        startTime.set(System.currentTimeMillis());

        String method = requestContext.getMethod();
        String path = uriInfo.getRequestUri().getPath();
        String query = uriInfo.getRequestUri().getQuery();
        String userAgent = requestContext.getHeaderString("User-Agent");
        String clientIP = getClientIP(requestContext);

        String fullPath = query != null ? path + "?" + query : path;

        logger.info("▶️ REQUEST: {} {} from {} (User-Agent: {})",
                method, fullPath, clientIP, userAgent);

        // Log headers for debugging (only in DEBUG mode)
        if (logger.isDebugEnabled()) {
            requestContext.getHeaders().forEach((key, values) ->
                    logger.debug("Header: {} = {}", key, values));
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        Long start = startTime.get();
        if (start != null) {
            long duration = System.currentTimeMillis() - start;
            String method = requestContext.getMethod();
            String path = uriInfo.getRequestUri().getPath();
            int status = responseContext.getStatus();

            String emoji = getStatusEmoji(status);
            String level = getLogLevel(status);

            switch (level) {
                case "ERROR":
                    logger.error("{} RESPONSE: {} {} -> {} ({}ms)",
                            emoji, method, path, status, duration);
                    break;
                case "WARN":
                    logger.warn("{} RESPONSE: {} {} -> {} ({}ms)",
                            emoji, method, path, status, duration);
                    break;
                default:
                    logger.info("{} RESPONSE: {} {} -> {} ({}ms)",
                            emoji, method, path, status, duration);
            }

            startTime.remove();
        }
    }

    private String getClientIP(ContainerRequestContext requestContext) {
        String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = requestContext.getHeaderString("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return requestContext.getHeaderString("Remote-Addr");
    }

    private String getStatusEmoji(int status) {
        if (status >= 200 && status < 300) return "✅";
        if (status >= 300 && status < 400) return "🔀";
        if (status >= 400 && status < 500) return "⚠️";
        if (status >= 500) return "❌";
        return "🔷";
    }

    private String getLogLevel(int status) {
        if (status >= 500) return "ERROR";
        if (status >= 400) return "WARN";
        return "INFO";
    }

    // Utility method for logging specific events
    public static void logSecurityEvent(String event, String username, String details) {
        logger.warn("🔐 SECURITY: {} - User: {} - Details: {}", event, username, details);
    }

    public static void logError(String operation, String details, Throwable throwable) {
        logger.error("💥 ERROR: {} - Details: {}", operation, details, throwable);
    }

    public static void logStreamEvent(String movie, String quality, String clientIP) {
        logger.info("🎬 STREAM: {} [{}] to {}", movie, quality, clientIP);
    }

    public static void logMeshEvent(String event, String file, String peer) {
        logger.info("🕸️ MESH: {} - File: {} - Peer: {}", event, file, peer);
    }
}