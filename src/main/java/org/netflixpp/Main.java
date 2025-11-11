package org.netflixpp;

import org.eclipse.jetty.server.ServerConnector;
import org.netflixpp.config.JettyConfig;
import org.netflixpp.config.NginxConfig;
import org.netflixpp.mesh.SeederServer;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Starting Netflix++ Server...");

            // 1. Create storage directories
            createStorageDirectories();

            // 2. Start NGINX reverse proxy
            startNginx();

            // 3. Start Mesh P2P Server
            startMeshServer();

            // 4. Start Jetty HTTP Server
            startJettyServer();

            System.out.println("✅ Netflix++ Server started successfully!");
            System.out.println("🌐 HTTP: http://localhost:80");
            System.out.println("🕸️ Mesh: port 9001");

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
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));
            System.out.println("📁 Storage directories created");
        } catch (Exception e) {
            System.err.println("⚠️ Could not create directories: " + e.getMessage());
        }
    }

    private static void startNginx() throws Exception {
        System.out.println("🔧 Starting NGINX...");
        NginxConfig.setupNginx();
        NginxConfig.startNginx();
        Thread.sleep(2000);
        System.out.println("✅ NGINX started on port 80");
    }

    private static void startMeshServer() {
        System.out.println("🕸️ Starting Mesh P2P Server...");
        new Thread(() -> {
            try {
                SeederServer server = new SeederServer(9001);
                server.start();
            } catch (Exception e) {
                System.err.println("❌ Mesh server failed: " + e.getMessage());
            }
        }).start();
        System.out.println("✅ Mesh server started on port 9001");
    }

    private static void startJettyServer() throws Exception {
        System.out.println("🛠️ Starting Jetty...");
        org.eclipse.jetty.server.Server server = JettyConfig.createServer(0);
        server.start();

        new Thread(() -> {
            try {
                server.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        System.out.println("✅ Jetty started on port "+ ((ServerConnector) server.getConnectors()[0]).getLocalPort());
    }
}