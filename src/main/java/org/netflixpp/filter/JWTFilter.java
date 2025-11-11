package org.netflixpp.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.netflixpp.config.ServerConfig;

import javax.crypto.SecretKey;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Key;

@Provider
public class JWTFilter implements ContainerRequestFilter {

    private static final String AUTHENTICATION_SCHEME = "Bearer";
    private final SecretKey key;

    public JWTFilter() {
        // Use the same secret as backend for compatibility
        String secret = ServerConfig.getJwtSecret();
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Skip authentication for public endpoints
        if (isPublicEndpoint(requestContext)) {
            return;
        }

        // Get the Authorization header from the request
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext, "Missing or invalid Authorization header");
            return;
        }

        // Extract the token from the Authorization header
        String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

        try {
            // Validate the token
            validateToken(token);

            // Extract user information and add to request context
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Add user info to request context for use in resources
            requestContext.setProperty("username", claims.getSubject());
            requestContext.setProperty("role", claims.get("role", String.class));

        } catch (Exception e) {
            abortWithUnauthorized(requestContext, "Invalid token: " + e.getMessage());
        }
    }

    private boolean isPublicEndpoint(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        // Public endpoints (no authentication required)
        return path.startsWith("auth/login") && "POST".equals(method) ||
                path.startsWith("auth/register") && "POST".equals(method) ||
                path.startsWith("health") && "GET".equals(method) ||
                path.startsWith("metrics") && "GET".equals(method) ||
                path.equals("movies") && "GET".equals(method) ||
                (path.startsWith("movies/") && path.endsWith("/stream") && "GET".equals(method));
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null &&
                authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void validateToken(String token) throws Exception {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception e) {
            throw new Exception("Token validation failed: " + e.getMessage());
        }
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"" + message + "\"}")
                .build());
    }

    // Utility method to get username from request context
    public static String getUsername(ContainerRequestContext context) {
        return (String) context.getProperty("username");
    }

    // Utility method to get role from request context
    public static String getRole(ContainerRequestContext context) {
        return (String) context.getProperty("role");
    }
}