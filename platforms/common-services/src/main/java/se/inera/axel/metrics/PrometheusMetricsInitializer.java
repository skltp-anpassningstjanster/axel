package se.inera.axel.metrics;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmInfoMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrometheusMetricsInitializer implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(PrometheusMetricsInitializer.class);
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static final String ENABLED_ENV = "PROMETHEUS_METRICS_ENABLED";
    private static final String PORT_ENV = "PROMETHEUS_METRICS_PORT";
    private static final String PATH_ENV = "PROMETHEUS_METRICS_PATH";
    private static final String HEALTH_PATH = "/health";
    private static final int DEFAULT_PORT = 8089;
    private static final String DEFAULT_PATH = "/prometheus";

    private static PrometheusMeterRegistry registry;
    private static HttpServer server;
    private static ExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (!isEnabled()) {
            LOG.info("Prometheus metrics endpoint disabled via {}", ENABLED_ENV);
            return;
        }

        if (!STARTED.compareAndSet(false, true)) {
            LOG.debug("Prometheus metrics endpoint already running, skipping initialization");
            return;
        }

        int port = resolvePort();
        String path = resolvePath();

        registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        bindStandardMetrics(registry);

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(path, exchange -> {
                try {
                    if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                        byte[] response = registry.scrape().getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
                        exchange.sendResponseHeaders(200, response.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response);
                        }
                    } else {
                        exchange.sendResponseHeaders(405, -1);
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to serve Prometheus scrape request", e);
                    exchange.sendResponseHeaders(500, -1);
                } finally {
                    exchange.close();
                }
            });
            server.createContext(HEALTH_PATH, exchange -> {
                try {
                    if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                        byte[] response = "Online".getBytes(StandardCharsets.UTF_8);
                        exchange.sendResponseHeaders(200, response.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response);
                        }
                    } else {
                        exchange.sendResponseHeaders(405, -1);
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to serve health request", e);
                    exchange.sendResponseHeaders(500, -1);
                } finally {
                    exchange.close();
                }
            });
            executor = Executors.newFixedThreadPool(2);
            server.setExecutor(executor);
            server.start();
            LOG.info("Prometheus metrics endpoint started on port {} at path {}", port, path);
        } catch (IOException e) {
            STARTED.set(false);
            LOG.error("Failed to start Prometheus metrics endpoint on port {} at path {}", port, path, e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (STARTED.compareAndSet(true, false)) {
            if (server != null) {
                server.stop(0);
                server = null;
            }
            if (executor != null) {
                executor.shutdownNow();
                executor = null;
            }
            if (registry != null) {
                registry.close();
                registry = null;
            }
            LOG.info("Prometheus metrics endpoint stopped");
        }
    }

    private boolean isEnabled() {
        String enabled = System.getenv(ENABLED_ENV);
        if (isNullOrEmpty(enabled)) {
            return true;
        }
        return !"false".equalsIgnoreCase(enabled.trim()) && !"0".equals(enabled.trim());
    }

    private int resolvePort() {
        String port = System.getenv(PORT_ENV);
        if (!isNullOrEmpty(port)) {
            try {
                return Integer.parseInt(port.trim());
            } catch (NumberFormatException e) {
                LOG.warn("Invalid {} '{}', falling back to default {}", PORT_ENV, port, DEFAULT_PORT);
            }
        }
        return DEFAULT_PORT;
    }

    private String resolvePath() {
        String path = System.getenv(PATH_ENV);
        if (isNullOrEmpty(path)) {
            return DEFAULT_PATH;
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private void bindStandardMetrics(PrometheusMeterRegistry targetRegistry) {
        new ClassLoaderMetrics().bindTo(targetRegistry);
        new JvmMemoryMetrics().bindTo(targetRegistry);
        new JvmGcMetrics().bindTo(targetRegistry);
        new JvmInfoMetrics().bindTo(targetRegistry);
        new JvmThreadMetrics().bindTo(targetRegistry);
        new ProcessorMetrics().bindTo(targetRegistry);
        new FileDescriptorMetrics().bindTo(targetRegistry);
        new UptimeMetrics().bindTo(targetRegistry);
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
