package com.tickethub.infrastructure.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDatabaseFactory;

import com.mongodb.client.MongoClient;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;

@IntegrationTest
@DisplayName("LiquibaseMigration")
class LiquibaseMigrationIT extends ContainerSupport {

    private static final String CHANGELOG = "db/changelog/db.changelog-master.xml";

    @Autowired
    MongoClient client;
    @Autowired
    MongoDatabaseFactory factory;

    @Test
    @DisplayName("Given startup, when migrate, then applies changes once and preserves data")
    void migratesOnStartupAndDoesNotReapplyChangesOrCloseClient() throws Exception {
        final var database = factory.getMongoDatabase();
        final var collections = database.listCollectionNames().into(new ArrayList<>());
        assertTrue(collections.containsAll(
                List.of("customers", "partners", "shows", "sections", "spots", "orders", "tickets")),
                () -> "Database should contain all expected collections after migration");
        final var history = database.getCollection("DATABASECHANGELOG");
        assertEquals(16, history.countDocuments(),
                () -> "Changelog history should contain 16 applied changesets");
        final var appliedIds = history.find()
                .into(new ArrayList<>())
                .stream()
                .map(document -> document.getString("id"))
                .toList();
        assertTrue(appliedIds.containsAll(List.of(
                "001-1-create-customers",
                "002-1-create-indexes",
                "003-1-create-audit-logs",
                "003-2-create-audit-logs-indexes",
                "004-1-ownership-links-indexes",
                "005-1-auth-credentials-indexes",
                "006-1-create-refresh-sessions",
                "006-2-create-refresh-sessions-indexes",
                "007-1-create-orders",
                "007-2-create-tickets",
                "007-3-orders-tickets-indexes",
                "002-2-orders-idempotency-index")),
                () -> "Changelog history should contain all expected changeset ids");
        database.getCollection("customers").insertOne(new Document("_id", "preserved"));
        LiquibaseConfiguration.migrate(client, factory, CHANGELOG);
        assertEquals(16, history.countDocuments(),
                () -> "Re-running migration should not reapply changesets");
        assertNotNull(database.getCollection("customers").find(new Document("_id", "preserved")).first(),
                () -> "Re-running migration should preserve existing customer data");
        assertEquals(1.0, database.runCommand(new Document("ping", 1)).getDouble("ok"),
                () -> "Mongo client should remain usable after migration");
    }

    @Test
    @DisplayName("Given missing changelog, when migrate, then propagates migration failure")
    void propagatesMigrationFailures() {
        final var exception = assertThrows(Exception.class,
                () -> LiquibaseConfiguration.migrate(client, factory, "missing-changelog.xml"),
                () -> "Migrating with a missing changelog should throw");

        assertNotNull(exception,
                () -> "Migration failure should produce an exception");
    }
}
