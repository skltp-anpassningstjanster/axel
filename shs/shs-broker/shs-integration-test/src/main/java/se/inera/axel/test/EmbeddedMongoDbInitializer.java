package se.inera.axel.test;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

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
    private int mongoPort;
    private boolean useIpv6;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
    	
    	//TODO: method is running twice in newer version of Camel.
    	if(System.getProperty("shsRsPort") != null)
    		return;
    	
    	try {
        	startMongoDb(applicationContext.getEnvironment());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        String shsRsPort = String.valueOf(AvailablePortFinder.getNextAvailable());
        System.setProperty("shsRsPort", shsRsPort);
        System.setProperty("shsRsHttpEndpoint", String.format("jetty://http://localhost:%s", shsRsPort));
        System.setProperty("mongodb.uri", String.format("mongodb://localhost:%s/axel?w=1", mongoPort));
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
        mongoPort = Network.getFreeServerPort();
        useIpv6 = Network.localhostIsIPv6();

        MongodStarter runtime = MongodStarter.getDefaultInstance();
        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.V3_4)
                .net(new Net("localhost", mongoPort, useIpv6))
                .build();

        mongodExecutable = runtime.prepare(mongodConfig);
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
