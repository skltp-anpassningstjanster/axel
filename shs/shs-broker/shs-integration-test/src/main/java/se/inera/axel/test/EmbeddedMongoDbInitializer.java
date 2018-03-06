package se.inera.axel.test;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.test.AvailablePortFinder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Initializes an in memory directory service to use for testing the communication with the LDAP server.
 * <p/>
 * Register this class as the initializer in the ContextConfiguration annotation.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class EmbeddedMongoDbInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>,
        ApplicationListener<ContextClosedEvent> {
    private MongodProcess mongodProcess;
    private MongodExecutable mongodExecutable;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            startMongoDb(applicationContext.getEnvironment());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }


//        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
//        Map<String, Object> mongopropsMap = DynamicProperties.get();
//        String shsRsPort = String.valueOf(AvailablePortFinder.getNextAvailable());
//        mongopropsMap.put("shsRsPort", shsRsPort);
//        mongopropsMap.put("shsRsHttpEndpoint", String.format("jetty://http://0.0.0.0:%s", shsRsPort));
//        mongopropsMap.put("mongodb.uri", String.format("mongodb://localhost:%s/axel?w=1", mongodProcess.getConfig().net().getPort()));
//        mongopropsMap.put("remoteShsBrokerPort", String.valueOf(AvailablePortFinder.getNextAvailable()));
//        propertySources.addFirst(new MapPropertySource("mongoprops", mongopropsMap));
//
//        try {
//            propertySources.addLast(new ResourcePropertySource("classpath:axel-shs-broker.properties"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        String shsRsPort = String.valueOf(AvailablePortFinder.getNextAvailable());
        System.setProperty("shsRsPort", shsRsPort);
        System.setProperty("shsRsHttpEndpoint", String.format("jetty://http://0.0.0.0:%s", shsRsPort));
        System.setProperty("mongodb.uri", String.format("mongodb://localhost:%s/axel?w=1", mongodProcess.getConfig().net().getPort()));
        System.setProperty("remoteShsBrokerPort", String.valueOf(AvailablePortFinder.getNextAvailable()));

        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        try {
            propertySources.addLast(new ResourcePropertySource("classpath:axel-shs-broker.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        applicationContext.addApplicationListener(this);
    }

    public void startMongoDb(Environment environment) throws IOException {
        final MongodStarter runtime = MongodStarter.getInstance(new RuntimeConfigBuilder()
                .defaults(Command.MongoD)
                .build());
        mongodExecutable = runtime.prepare(new MongodConfigBuilder().version(Version.Main.PRODUCTION).build());
        mongodProcess = mongodExecutable.start();
    }

    public @Bean(destroyMethod = "stop") MongodExecutable mongodExecutable() throws Exception {

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(Command.MongoD)
                .artifactStore(new ArtifactStoreBuilder()
                        .defaults(Command.MongoD)
                        .executableNaming(new UUIDTempNaming())
                )
                .build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.V3_4)
                .net(new Net(Network.getFreeServerPort(), Network.localhostIsIPv6()))
                .build();

        return runtime.prepare(mongodConfig);
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (mongodProcess != null) {
            mongodProcess.stop();
        }

        if (mongodExecutable != null) {
            mongodExecutable.stop();
        }
    }
}
