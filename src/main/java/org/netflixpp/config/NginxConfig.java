package org.netflixpp.config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NginxConfig {

    private static final String NGINX_CONF = """
        worker_processes 1;
        error_log logs/nginx_error.log;
        pid logs/nginx.pid;
        
        events {
            worker_connections 1024;
        }
        
        http {
            include mime.types;
            default_type application/octet-stream;
            sendfile on;
            keepalive_timeout 65;
            
            # Upstream backend servers
            upstream backend_servers {
                server localhost:8080;
            }
            
            # Main server block
            server {
                listen 80;
                server_name localhost;
                
                # API routes - proxy to Jetty
                location /api/ {
                    proxy_pass http://backend_servers;
                    proxy_set_header Host $host;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                    proxy_set_header X-Forwarded-Proto $scheme;
                    
                    # CORS headers
                    add_header Access-Control-Allow-Origin *;
                    add_header Access-Control-Allow-Methods 'GET, POST, PUT, DELETE, OPTIONS';
                    add_header Access-Control-Allow-Headers 'Origin, Content-Type, Accept, Authorization, Range';
                    add_header Access-Control-Expose-Headers 'Content-Range, Accept-Ranges';
                    
                    # Handle preflight requests
                    if ($request_method = 'OPTIONS') {
                        add_header Access-Control-Allow-Origin *;
                        add_header Access-Control-Allow-Methods 'GET, POST, PUT, DELETE, OPTIONS';
                        add_header Access-Control-Allow-Headers 'Origin, Content-Type, Accept, Authorization, Range';
                        add_header Access-Control-Max-Age 86400;
                        return 204;
                    }
                }
                
                # Static file serving for videos
                location /storage/ {
                    alias storage/;
                    add_header Access-Control-Allow-Origin *;
                    add_header Access-Control-Allow-Methods 'GET, OPTIONS';
                    add_header Access-Control-Allow-Headers 'Range';
                    add_header Access-Control-Expose-Headers 'Content-Range, Accept-Ranges';
                    
                    # Video streaming support
                    mp4;
                    mp4_buffer_size 1m;
                    mp4_max_buffer_size 5m;
                    
                    # Range requests for video streaming
                    proxy_set_header Range $http_range;
                    proxy_set_header If-Range $http_if_range;
                    proxy_no_cache $http_range $http_if_range;
                }
                
                # Root redirect to API
                location / {
                    return 302 /api/;
                }
            }
        }
        """;

    public static void setupNginx() throws IOException {
        // Create nginx directory
        Files.createDirectories(Paths.get("nginx"));

        // Write nginx configuration
        Files.writeString(Paths.get("nginx/nginx.conf"), NGINX_CONF);

        System.out.println("📄 NGINX configuration created");
    }

    public static void startNginx() throws IOException, InterruptedException {
        // Check if nginx is installed
        ProcessBuilder checkPb = new ProcessBuilder("nginx", "-v");
        try {
            Process checkProcess = checkPb.start();
            if (checkProcess.waitFor() != 0) {
                throw new IOException("NGINX not found. Please install NGINX.");
            }
        } catch (IOException e) {
            throw new IOException("NGINX not installed or not in PATH. Please install NGINX.");
        }

        // Start nginx with our config
        ProcessBuilder pb = new ProcessBuilder(
                "nginx", "-c", Paths.get("nginx/nginx.conf").toAbsolutePath().toString(),
                "-p", Paths.get(".").toAbsolutePath().toString()
        );

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("NGINX Error: " + line);
                }
            }
            throw new IOException("Failed to start NGINX. Exit code: " + exitCode);
        }
    }

    public static void stopNginx() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "nginx", "-s", "stop",
                "-c", Paths.get("nginx/nginx.conf").toAbsolutePath().toString(),
                "-p", Paths.get(".").toAbsolutePath().toString()
        );

        Process process = pb.start();
        process.waitFor();
    }
}