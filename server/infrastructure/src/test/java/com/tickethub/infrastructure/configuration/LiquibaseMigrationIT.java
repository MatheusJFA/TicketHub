package com.tickethub.infrastructure.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDatabaseFactory;

import com.mongodb.client.MongoClient;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;

@IntegrationTest
class LiquibaseMigrationIT extends ContainerSupport {

    private static final String CHANGELOG = "db/changelog/db.changelog-master.xml";

    @Autowired
    MongoClient client;
    @Autowired
    MongoDatabaseFactory factory;

    @Test
    void migratesOnStartupAndDoesNotReapplyChangesOrCloseClient() throws Exception {
        final var database = factory.getMongoDatabase();
        final var collections = database.listCollectionNames().into(new ArrayList<>());
        assertTrue(collections.containsAll(
                List.of("customers", "partners", "shows", "sections", "spots")));
        final var history = database.getCollection("DATABASECHANGELOG");
        assertEquals(8, history.countDocuments());
        final var appliedIds = history.find()
                .into(new ArrayList<>())
                .stream()
                .map(document -> document.getString("id"))
                .toList();
        assertTrue(appliedIds.containsAll(List.of(
                "001-1-create-customers",
                "002-1-create-indexes",
                "003-1-create-audit-logs",
                "003-2-create-audit-logs-indexes")));
        database.getCollection("customers").insertOne(new Document("_id", "preserved"));
        LiquibaseConfiguration.migrate(client, factory, CHANGELOG);
        assertEquals(8, history.countDocuments());
        assertNotNull(database.getCollection("customers").find(new Document("_id", "preserved")).first());
        assertEquals(1.0, database.runCommand(new Document("ping", 1)).getDouble("ok"));
    }

    @Test
    void propagatesMigrationFailures() {
        assertThrows(Exception.class,
                () -> LiquibaseConfiguration.migrate(client, factory, "missing-changelog.xml"));
    }
}
