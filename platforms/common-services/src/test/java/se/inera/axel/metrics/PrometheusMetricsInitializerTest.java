package se.inera.axel.metrics;

import com.sun.net.httpserver.HttpServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import javax.servlet.ServletContextEvent;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class PrometheusMetricsInitializerTest {

    private final RecordingServerFactory serverFactory = new RecordingServerFactory();
    private PrometheusMetricsInitializer initializer;

    @AfterMethod
    public void tearDown() {
        if (initializer != null) {
            initializer.contextDestroyed((ServletContextEvent) null);
        }
        if (serverFactory.server != null) {
            serverFactory.server.stop(0);
            serverFactory.server = null;
        }
        PrometheusMetricsInitializer.resetForTests();
    }

    @Test
    public void isEnabledDefaultsToTrue() {
        initializer = newInitializer(new HashMap<>());
        assertTrue(initializer.isEnabled());
    }

    @Test
    public void isEnabledHandlesFalseAndZero() {
        Map<String, String> env = new HashMap<>();
        env.put("PROMETHEUS_METRICS_ENABLED", "false");
        initializer = newInitializer(env);
        assertFalse(initializer.isEnabled());

        env.put("PROMETHEUS_METRICS_ENABLED", "0");
        initializer = newInitializer(env);
        assertFalse(initializer.isEnabled());

        env.put("PROMETHEUS_METRICS_ENABLED", "TrUe ");
        initializer = newInitializer(env);
        assertTrue(initializer.isEnabled());
    }

    @Test
    public void resolvesPortFromEnvironmentOrDefault() {
        Map<String, String> env = new HashMap<>();
        initializer = newInitializer(env);
        assertEquals(initializer.resolvePort(), 8089);

        env.put("PROMETHEUS_METRICS_PORT", "9090");
        initializer = newInitializer(env);
        assertEquals(initializer.resolvePort(), 9090);

        env.put("PROMETHEUS_METRICS_PORT", "invalid");
        initializer = newInitializer(env);
        assertEquals(initializer.resolvePort(), 8089);
    }

    @Test
    public void resolvesPathWithLeadingSlash() {
        Map<String, String> env = new HashMap<>();
        initializer = newInitializer(env);
        assertEquals(initializer.resolvePath(), "/prometheus");

        env.put("PROMETHEUS_METRICS_PATH", "custom");
        initializer = newInitializer(env);
        assertEquals(initializer.resolvePath(), "/custom");

        env.put("PROMETHEUS_METRICS_PATH", "/ready");
        initializer = newInitializer(env);
        assertEquals(initializer.resolvePath(), "/ready");
    }

    @Test
    public void detectsEmptyStrings() {
        initializer = newInitializer(new HashMap<>());
        assertTrue(initializer.isNullOrEmpty(null));
        assertTrue(initializer.isNullOrEmpty(""));
        assertTrue(initializer.isNullOrEmpty("  "));
        assertFalse(initializer.isNullOrEmpty("value"));
    }

    @Test
    public void parsesPortOrFallsBackToDefault() {
        initializer = newInitializer(new HashMap<>());
        assertEquals(initializer.parsePortOrDefault("8080"), 8080);
        assertEquals(initializer.parsePortOrDefault(" not-a-number "), 8089);
    }

    @Test
    public void startsAndStopsServerWithEndpoints() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("PROMETHEUS_METRICS_ENABLED", "true");
        env.put("PROMETHEUS_METRICS_PORT", "0"); // let OS pick a free port
        env.put("PROMETHEUS_METRICS_PATH", "metrics");

        initializer = newInitializer(env);
        initializer.contextInitialized((ServletContextEvent) null);

        int port = serverFactory.getBoundPort();
        assertTrue(port > 0, "Server should be bound to an ephemeral port");

        String metricsBody = get("/metrics", port);
        assertNotNull(metricsBody);
        assertTrue(metricsBody.contains("# HELP"));

        String healthBody = get("/health", port);
        assertEquals(healthBody, "Online");

        initializer.contextDestroyed((ServletContextEvent) null);
    }

    @Test
    public void doesNotStartTwice() {
        Map<String, String> env = new HashMap<>();
        env.put("PROMETHEUS_METRICS_ENABLED", "true");
        env.put("PROMETHEUS_METRICS_PORT", "0");

        initializer = newInitializer(env);
        initializer.contextInitialized((ServletContextEvent) null);
        initializer.contextInitialized((ServletContextEvent) null);

        assertEquals(serverFactory.createdCount, 1, "Server should be created only once");
    }

    @Test
    public void returnsServerErrorWhenSupplierFails() throws Exception {
        initializer = newInitializer(new HashMap<>());
        HttpServer localServer = HttpServer.create(new InetSocketAddress(0), 0);
        initializer.registerGetContext(localServer, "/boom", () -> {
            throw new IllegalStateException("boom");
        }, "text/plain");
        localServer.start();
        int port = localServer.getAddress().getPort();

        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + port + "/boom").openConnection();
        connection.setRequestMethod("GET");
        assertEquals(connection.getResponseCode(), 500);
        localServer.stop(0);
    }

    private PrometheusMetricsInitializer newInitializer(Map<String, String> env) {
        MapEnvironmentReader reader = new MapEnvironmentReader(env);
        return new PrometheusMetricsInitializer(reader, serverFactory);
    }

    private String get(String path, int port) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + port + path).openConnection();
        connection.setRequestMethod("GET");
        assertEquals(connection.getResponseCode(), 200);
        byte[] buffer = new byte[1024];
        int read;
        try (java.io.InputStream in = connection.getInputStream();
             java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static class MapEnvironmentReader implements PrometheusMetricsInitializer.EnvironmentReader {
        private final Map<String, String> values;

        MapEnvironmentReader(Map<String, String> values) {
            this.values = values;
        }

        @Override
        public String get(String key) {
            return values.get(key);
        }
    }

    private static class RecordingServerFactory implements PrometheusMetricsInitializer.HttpServerFactory {
        HttpServer server;
        int createdCount;

        @Override
        public HttpServer create(int port) throws IOException {
            createdCount++;
            server = HttpServer.create(new InetSocketAddress(port), 0);
            return server;
        }

        int getBoundPort() {
            if (server == null) {
                return -1;
            }
            return server.getAddress().getPort();
        }
    }
}
