package org.netflixpp;

import org.eclipse.jetty.server.Server;
import org.netflixpp.config.JettyConfig;
import org.netflixpp.config.NginxConfig;

public class Main {
    private static final int API_PORT = 8080;
    private static final int NGINX_PORT = 80;

    public static void main(String[] args) {
        try {
            System.out.println("🚀 Starting Netflix++ Server...");

            // 1. Create necessary directories
            createStorageDirectories();

            // 2. Start NGINX reverse proxy
            startNginx();

            // 3. Start Jetty application server
            startJettyServer();

            System.out.println("✅ Netflix++ Server started successfully!");
            System.out.println("🌐 API Gateway: http://localhost:" + NGINX_PORT);
            System.out.println("🔧 Application Server: http://localhost:" + API_PORT);
            System.out.println("📹 Streaming: http://localhost:" + NGINX_PORT + "/storage/");

        } catch (Exception e) {
            System.err.println("❌ Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void createStorageDirectories() {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("storage/movies"));
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("storage/chunks"));
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("storage/temp"));
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));
            System.out.println("📁 Storage directories created");
        } catch (Exception e) {
            System.err.println("⚠️ Could not create storage directories: " + e.getMessage());
        }
    }

    private static void startNginx() throws Exception {
        System.out.println("🔧 Starting NGINX reverse proxy...");
        NginxConfig.setupNginx();
        NginxConfig.startNginx();
        Thread.sleep(2000); // Wait for NGINX to start
        System.out.println("✅ NGINX started on port " + NGINX_PORT);
    }

    private static void startJettyServer() throws Exception {
        System.out.println("🛠️ Starting Jetty application server...");
        Server server = JettyConfig.createServer(API_PORT);
        server.start();

        // Run server in background thread
        new Thread(() -> {
            try {
                server.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        System.out.println("✅ Jetty server started on port " + API_PORT);
    }
}