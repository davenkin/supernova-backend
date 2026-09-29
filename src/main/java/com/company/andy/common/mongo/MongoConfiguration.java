package com.company.andy.common.mongo;

import io.mongock.runner.springboot.EnableMongock;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.persistence.autoconfigure.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoManagedTypes;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.transaction.PlatformTransactionManager;

import static com.mongodb.ReadPreference.secondaryPreferred;
import static com.mongodb.WriteConcern.MAJORITY;
import static org.springframework.data.mongodb.core.WriteResultChecking.EXCEPTION;

@EnableMongock
@Configuration(proxyBeanMethods = false)
public class MongoConfiguration {

    // Make all @Persistent(including @Document and @TypeAlias) annotated class to be managed by Spring Mongo,
    // otherwise only @Document annotated classes are managed by Spring Mongo, but not @TypeAlias annotated classes
    @Bean
    MongoManagedTypes mongoManagedTypes(ApplicationContext applicationContext) throws ClassNotFoundException {
        return MongoManagedTypes.fromIterable(new EntityScanner(applicationContext).scan(Persistent.class));
    }

    @Bean
    public PlatformTransactionManager transactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTransactionManager(mongoDatabaseFactory);
    }

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return builder -> {
            builder.applyToConnectionPoolSettings(poolBuilder -> {
                poolBuilder.maxSize(500).minSize(5);
            });
        };
    }

    @Bean
    MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MongoConverter converter) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory, converter);
        mongoTemplate.setWriteConcern(MAJORITY);
        mongoTemplate.setWriteConcernResolver(action -> MAJORITY);
        mongoTemplate.setWriteResultChecking(EXCEPTION);
        mongoTemplate.setReadPreference(secondaryPreferred());
        return mongoTemplate;
    }

}
