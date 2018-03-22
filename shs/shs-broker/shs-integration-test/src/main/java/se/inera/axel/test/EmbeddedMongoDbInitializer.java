package se.inera.axel.test;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import se.inera.axel.test.flapdoodle.FixedTempNaming;

import org.apache.camel.test.AvailablePortFinder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

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

        String shsRsPort = String.valueOf(AvailablePortFinder.getNextAvailable());
        System.setProperty("shsRsPort", shsRsPort);
        System.setProperty("shsRsHttpEndpoint", String.format("jetty://http://localhost:%s", shsRsPort));
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
                .artifactStore(new ExtractedArtifactStoreBuilder()
                .defaults(Command.MongoD)
                .executableNaming(new FixedTempNaming("shs-integration-mongodb"))
                )
                .build());
        mongodExecutable = runtime.prepare(
        		new MongodConfigBuilder()
        		.version(Version.Main.V3_4)
                .net(new Net(Network.getFreeServerPort(), Network.localhostIsIPv6()))
      		.build());
        mongodProcess = mongodExecutable.start();
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
