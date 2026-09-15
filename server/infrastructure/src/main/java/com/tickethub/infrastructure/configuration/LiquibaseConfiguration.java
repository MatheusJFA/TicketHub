package com.tickethub.infrastructure.configuration;

import com.mongodb.client.MongoClient;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.ext.mongodb.database.MongoConnection;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import liquibase.ext.mongodb.statement.BsonUtils;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "tickethub.liquibase", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LiquibaseConfiguration {
    @Bean
    InitializingBean mongoMigrations(MongoClient client, MongoDatabaseFactory factory,
            @Value("${tickethub.liquibase.change-log}") String changeLog) {
        return () -> migrate(client, factory, changeLog);
    }

    public static void migrate(MongoClient client, MongoDatabaseFactory factory, String changeLog) throws Exception {
        // Spring owns the client lifecycle, including credentials, TLS and timeouts.
        final var connection = new MongoConnection() {
            @Override
            public void close() {
                // Liquibase must not close the shared application client.
            }

            @Override
            public String getVisibleUrl() {
                return "MongoDB database: " + getMongoDatabase().getName();
            }
        };
        connection.setMongoClient(client);
        connection.setMongoDatabase(factory.getMongoDatabase().withCodecRegistry(BsonUtils.uuidCodecRegistry()));
        final var database = new MongoLiquibaseDatabase();
        database.setConnection(connection);
        try (final var resources = new ClassLoaderResourceAccessor();
             final var liquibase = new Liquibase(changeLog, resources, database)) {
            liquibase.update(new Contexts(), new LabelExpression());
        }
    }
}
