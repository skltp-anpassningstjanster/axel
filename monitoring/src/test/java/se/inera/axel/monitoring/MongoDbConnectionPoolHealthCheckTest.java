package se.inera.axel.monitoring;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.management.JMXConnectionPoolListener;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.testng.annotations.*;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class MongoDbConnectionPoolHealthCheckTest extends AbstractHealthCheckTest {

    public static final String MONGODB_NAME_PATTERN = "org.mongodb.driver:type=ConnectionPool,*";
    private MongoClient mongoClient;

    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private MongodExecutable mongodExecutable;
    private MongodProcess mongodProcess;
    private int port;

    @BeforeClass
    public void beforeClass() throws Exception {
        port = Network.getFreeServerPort();
        mongodExecutable = starter.prepare(createMongodConfig());
        mongodProcess = mongodExecutable.start();
        mBeanServer = ManagementFactory.getPlatformMBeanServer();
    }


    @AfterClass
    public void tearDown() throws Exception {
        if (mongodProcess != null) mongodProcess.stop();
        if (mongodExecutable != null) mongodExecutable.stop();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        JMXConnectionPoolListener connectionPoolListener = new JMXConnectionPoolListener();
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder()
                .addConnectionPoolListener(connectionPoolListener)
                .build();
        mongoClient = new MongoClient(new ServerAddress("localhost", port), mongoClientOptions);
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        mongoClient.close();
    }

    @Test
    public void whenNoConnectionIsInUseAHealthStatusShouldNotBeReported() throws Exception {
        MongoDbConnectionPoolHealthCheck healthCheck =
                new MongoDbConnectionPoolHealthCheck("se.inera.axel.test", MONGODB_NAME_PATTERN, null);

        List<HealthStatus> healthStatuses = new ArrayList<>();
        healthCheck.check(healthStatuses, mBeanServer);

        assertThat(healthStatuses, is(empty()));
    }

    @Test
    public void allConnectionsInUseShouldTriggerWarning() throws Exception {

        int maxConnectionsPerHost = mongoClient.getMongoClientOptions().getConnectionsPerHost();
        callDbInParallelMultipleTimes(maxConnectionsPerHost);

        MongoDbConnectionPoolHealthCheck healthCheck =
                new MongoDbConnectionPoolHealthCheck("se.inera.axel.test", MONGODB_NAME_PATTERN, null);

        List<HealthStatus> healthStatuses = new ArrayList<>();
        healthCheck.check(healthStatuses, mBeanServer);

        assertThat(healthStatuses, contains(hasProperty("level", equalTo(SeverityLevel.WARNING))));

    }

    void callDbInParallelMultipleTimes(int timesToCallDatabase) throws InterruptedException, ExecutionException {
        Collection<Callable<String>> tasks = new ArrayList<>();
        for(int i=0; i<timesToCallDatabase;i++){
            tasks.add(new CallDatabaseTask(""+i));
        }

        ExecutorService executor = Executors.newFixedThreadPool(timesToCallDatabase);
        executor.invokeAll(tasks);
        executor.shutdown();
    }

    private final class CallDatabaseTask implements Callable {
        CallDatabaseTask(String name){
            this.name = name;
        }
         @Override public String call() throws Exception {
            mongoClient.getDatabase(name).createCollection(name);
            return name;
        }
        private final String name;
    }

    private MongodConfig createMongodConfig() throws IOException {
        return MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net("localhost", port, Network.localhostIsIPv6()))
                .build();
    }
}
