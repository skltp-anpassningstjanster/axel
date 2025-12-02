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
package se.inera.axel.riv.impl;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

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

    MongoClient mongo;
    MongodProcess mongodProcess;
    private int mongoPort;
    private boolean useIpv6;
    
    public @Bean(destroyMethod = "stop") MongodExecutable mongodExecutable() throws Exception {
        mongoPort = Network.getFreeServerPort();
        useIpv6 = Network.localhostIsIPv6();

        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.V3_4)
                .net(new Net("localhost", mongoPort, useIpv6))
                .build();

        MongodStarter runtime = MongodStarter.getDefaultInstance();

        return runtime.prepare(mongodConfig);
    }

    public @Bean(destroyMethod = "stop") MongodProcess mongodProcess() throws Exception {

    	mongodProcess = mongodExecutable().start();

        return  mongodProcess;
    }

    public @Bean(destroyMethod = "close") MongoClient mongo() throws Exception {
        mongo = new MongoClient(new ServerAddress("localhost", mongoPort));
        return mongo;
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

    @Override
    public void destroy() throws Exception {

        if (mongo != null)
            mongo.close();
        
        if (mongodProcess != null)
            mongodProcess.stop();
    }
    
}
