/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.broker.agreement.mongo;

import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

@Configuration
public class MongoDBTestContextConfig implements DisposableBean {

    public @Bean(destroyMethod = "stop") MongodExecutable mongodExecutable() throws Exception {
        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.V2_2)
                .net(new Net("127.0.0.1", Network.getFreeServerPort(), Network.localhostIsIPv6()))
                .build();

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(Command.MongoD)
                .artifactStore(new ArtifactStoreBuilder()
                        .defaults(Command.MongoD)
                        .executableNaming(new FixedTempNaming())
                )
                .build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

        return runtime.prepare(mongodConfig);
    }

    public @Bean(destroyMethod = "stop") MongodProcess mongodProcess() throws Exception {

        MongodProcess mongod = mongodExecutable().start();

        return  mongod;
    }

    @SuppressWarnings("deprecation")
	public @Bean(destroyMethod = "close") Mongo mongo() throws Exception {
        MongodProcess mongodProcess = mongodProcess();

        return new Mongo(new ServerAddress(mongodProcess.getConfig().net().getServerAddress(), mongodProcess.getConfig().net().getPort()));
    }

    public @Bean MongoDbFactory mongoDbFactory() throws Exception {
        return new SimpleMongoDbFactory(mongo(), "axel-test");
    }

    public @Bean MongoOperations mongoOperations() throws Exception {
        return new MongoTemplate(mongoDbFactory());
    }

    public @Bean MongoRepositoryFactory mongoRepositoryFactory() throws Exception {
        return new MongoRepositoryFactory(mongoOperations());
    }

    public @Bean MongoShsAgreementRepository mongoShsAgreementRepository() throws Exception {
            return mongoRepositoryFactory().getRepository(MongoShsAgreementRepository.class);
    }

    public @Bean AgreementAssembler agreementAssembler() throws Exception {
               return new AgreementAssembler();
       }

    @Override
    public void destroy() throws Exception {
        Mongo mongo = mongo();

        if (mongo != null)
            mongo.close();
        
        MongodProcess mongodProcess = mongodProcess();

        if (mongodProcess != null)
            mongodProcess.stop();
    }
    private class FixedTempNaming implements ITempNaming {

    	@Override
    	public String nameFor(String prefix, String postfix) {
    		final String name = prefix + "-" + "shs-agreement-mongodb" + "-" + postfix;
    		
    	    String tempFile = System.getenv("temp") + File.separator + name;

    	    deleteFile(name);
    		return name;
    	}
    	private void deleteFile(String name) {
    		// Temporary fix. Needs refactoring
    	    String tempFile = System.getenv("temp") + File.separator + name;

    	    try {
				Files.deleteIfExists(new File(tempFile).toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}    
    	}
    }
}
