package se.inera.axel.metrics;

import com.sun.net.httpserver.HttpServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class PrometheusMetricsInitializerTest {

    private static final String ENABLED_ENV = "PROMETHEUS_METRICS_ENABLED";
    private static final String PORT_ENV = "PROMETHEUS_METRICS_PORT";
    private static final String PATH_ENV = "PROMETHEUS_METRICS_PATH";

    private final Map<String, String> originalEnv = new HashMap<>();

    @BeforeMethod
    public void setUp() throws Exception {
        captureOriginalEnv();
        clearTestEnvs();
        resetStartedFlag();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        restoreEnv();
        resetStaticFields();
    }

    @Test
    public void shouldEnableMetricsByDefault() throws Exception {
        PrometheusMetricsInitializer initializer = new PrometheusMetricsInitializer();
        boolean enabled = (Boolean) invokePrivate(initializer, "isEnabled");

        assertTrue(enabled);
    }

    @Test
    public void shouldDisableMetricsWhenEnvFalse() throws Exception {
        setEnvVar(ENABLED_ENV, "false");
        PrometheusMetricsInitializer initializer = new PrometheusMetricsInitializer();

        boolean enabled = (Boolean) invokePrivate(initializer, "isEnabled");

        assertFalse(enabled);
    }

    @Test
    public void shouldResolvePortFromEnv() throws Exception {
        setEnvVar(PORT_ENV, "9090");
        PrometheusMetricsInitializer initializer = new PrometheusMetricsInitializer();

        int port = (Integer) invokePrivate(initializer, "resolvePort");

        assertEquals(port, 9090);
    }

    @Test
    public void shouldFallbackToDefaultPortOnInvalidEnv() throws Exception {
        setEnvVar(PORT_ENV, "not-a-number");
        PrometheusMetricsInitializer initializer = new PrometheusMetricsInitializer();

        int port = (Integer) invokePrivate(initializer, "resolvePort");

        assertEquals(port, 8089);
    }

    @Test
    public void shouldResolvePathWithLeadingSlash() throws Exception {
        PrometheusMetricsInitializer initializer = new PrometheusMetricsInitializer();
        String defaultPath = (String) invokePrivate(initializer, "resolvePath");

        assertEquals(defaultPath, "/prometheus");
    }

    @Test
    public void shouldResolvePathWithoutAddingDoubleSlash() throws Exception {
        setEnvVar(PATH_ENV, "metrics");
        PrometheusMetricsInitializer initializer = new PrometheusMetricsInitializer();

        String path = (String) invokePrivate(initializer, "resolvePath");

        assertEquals(path, "/metrics");
    }

    @Test
    public void shouldServeContentFromRegisteredContext() throws Exception {
        PrometheusMetricsInitializer initializer = new PrometheusMetricsInitializer();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        Method register = PrometheusMetricsInitializer.class.getDeclaredMethod("registerGetContext", HttpServer.class, String.class, Supplier.class, String.class);
        register.setAccessible(true);
        register.invoke(initializer, server, "/test", (Supplier<byte[]>) () -> "hello".getBytes(StandardCharsets.UTF_8), "text/plain; charset=utf-8");
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();

        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + port + "/test").openConnection();
        connection.setRequestMethod("GET");

        assertEquals(connection.getResponseCode(), 200);
        assertEquals(readFully(connection.getInputStream()), "hello");
        assertEquals(connection.getHeaderField("Content-Type"), "text/plain; charset=utf-8");

        HttpURLConnection postConnection = (HttpURLConnection) new URL("http://localhost:" + port + "/test").openConnection();
        postConnection.setRequestMethod("POST");
        assertEquals(postConnection.getResponseCode(), 405);

        server.stop(0);
        ((ExecutorService) server.getExecutor()).shutdownNow();
    }

    @Test
    public void shouldCleanupResourcesOnContextDestroyed() throws Exception {
        PrometheusMetricsInitializer initializer = new PrometheusMetricsInitializer();
        AtomicBoolean started = (AtomicBoolean) getField("STARTED");
        started.set(true);

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        setField("server", server);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        setField("executor", executor);
        Object registry = Class.forName("io.micrometer.prometheus.PrometheusMeterRegistry")
                .getConstructor(io.micrometer.prometheus.PrometheusConfig.class)
                .newInstance(io.micrometer.prometheus.PrometheusConfig.DEFAULT);
        setField("registry", registry);

        initializer.contextDestroyed(null);

        assertNull(getField("server"));
        assertNull(getField("executor"));
        assertNull(getField("registry"));
        assertFalse(started.get());
    }

    private void captureOriginalEnv() {
        originalEnv.clear();
        originalEnv.putAll(System.getenv());
    }

    private void clearTestEnvs() throws Exception {
        setEnvVar(ENABLED_ENV, null);
        setEnvVar(PORT_ENV, null);
        setEnvVar(PATH_ENV, null);
    }

    private void restoreEnv() throws Exception {
        Map<String, String> target = new HashMap<>(originalEnv);
        setEnv(target);
    }

    private void resetStartedFlag() throws Exception {
        AtomicBoolean started = (AtomicBoolean) getField("STARTED");
        started.set(false);
    }

    private void resetStaticFields() throws Exception {
        setField("server", null);
        setField("executor", null);
        setField("registry", null);
    }

    private Object invokePrivate(Object target, String methodName, Object... args) throws Exception {
        Method method = PrometheusMetricsInitializer.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private Object getField(String name) throws Exception {
        Field field = PrometheusMetricsInitializer.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = PrometheusMetricsInitializer.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    private void setEnvVar(String key, String value) throws Exception {
        Map<String, String> newEnv = new HashMap<>(System.getenv());
        if (value == null) {
            newEnv.remove(key);
        } else {
            newEnv.put(key, value);
        }
        setEnv(newEnv);
    }

    private void setEnv(Map<String, String> newEnv) throws Exception {
        try {
            Class<?> processEnvironment = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironment.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.clear();
            env.putAll(newEnv);

            Field caseInsensitiveField = processEnvironment.getDeclaredField("theCaseInsensitiveEnvironment");
            caseInsensitiveField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, String> ciEnv = (Map<String, String>) caseInsensitiveField.get(null);
            if (ciEnv != null) {
                ciEnv.clear();
                ciEnv.putAll(newEnv);
            }
        } catch (NoSuchFieldException e) {
            Map<String, String> env = System.getenv();
            Class<?> unmodifiableMapClass = Class.forName("java.util.Collections$UnmodifiableMap");
            Field field = unmodifiableMapClass.getDeclaredField("m");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, String> modifiable = (Map<String, String>) field.get(env);
            modifiable.clear();
            modifiable.putAll(newEnv);
        }
    }

    private String readFully(InputStream inputStream) throws Exception {
        try (InputStream is = inputStream; ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
