package com.tickethub.infrastructure.shared.persistence;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.DeleteManyModel;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;

/**
 * Collects inserts, replacements and removals across collections and flushes
 * them with one bulk write per collection ({@code ordered=false}).
 *
 * <p>A unit of work is short-lived: gateways create one per mutating call, so
 * no shared state leaks between requests. Call {@link #commit()} once;
 * {@link #rollback()} discards everything without touching the database.
 *
 * <p>Bulk writes are efficient but not atomic across documents — true
 * multi-document atomicity requires a MongoDB replica set. Replacements use
 * upsert so new children (e.g. sections added to a show) persist on update.
 */
public final class MongoUnitOfWork {

    private final MongoTemplate mongoTemplate;
    private final Map<String, List<WriteModel<Document>>> models = new LinkedHashMap<>();

    public MongoUnitOfWork(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
    }

    public <T> MongoUnitOfWork registerNew(final String collection, final T document) {
        Objects.requireNonNull(collection, "'collection' should not be null");
        Objects.requireNonNull(document, "'document' should not be null");
        pending(collection).add(new InsertOneModel<>(toBson(document)));
        return this;
    }

    public <T> MongoUnitOfWork registerDirty(final String collection, final Object id, final T document) {
        Objects.requireNonNull(collection, "'collection' should not be null");
        Objects.requireNonNull(id, "'id' should not be null");
        Objects.requireNonNull(document, "'document' should not be null");
        pending(collection).add(new ReplaceOneModel<>(eq("_id", id), toBson(document),
                new ReplaceOptions().upsert(true)));
        return this;
    }

    public MongoUnitOfWork registerRemoved(final String collection, final Object id) {
        Objects.requireNonNull(collection, "'collection' should not be null");
        Objects.requireNonNull(id, "'id' should not be null");
        pending(collection).add(new DeleteOneModel<>(eq("_id", id)));
        return this;
    }

    public MongoUnitOfWork registerRemoved(final String collection, final List<?> ids) {
        Objects.requireNonNull(collection, "'collection' should not be null");
        Objects.requireNonNull(ids, "'ids' should not be null");
        if (!ids.isEmpty()) {
            pending(collection).add(new DeleteManyModel<>(in("_id", ids)));
        }
        return this;
    }

    public boolean isEmpty() {
        return models.values().stream().allMatch(List::isEmpty);
    }

    public int pendingOperations() {
        return models.values().stream().mapToInt(List::size).sum();
    }

    public void commit() {
        try {
            final var options = new BulkWriteOptions().ordered(false);
            for (final Map.Entry<String, List<WriteModel<Document>>> entry : models.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    mongoTemplate.getCollection(entry.getKey()).bulkWrite(entry.getValue(), options);
                }
            }
        } finally {
            rollback();
        }
    }

    public void rollback() {
        models.clear();
    }

    private List<WriteModel<Document>> pending(final String collection) {
        return models.computeIfAbsent(collection, key -> new ArrayList<>());
    }

    private Document toBson(final Object document) {
        final var bson = new Document();
        mongoTemplate.getConverter().write(document, bson);
        return bson;
    }
}
