package com.tickethub.infrastructure.operator;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.core.operator.Operator;
import com.tickethub.domain.core.operator.OperatorGateway;
import com.tickethub.domain.core.operator.OperatorID;
import com.tickethub.domain.shared.Email;
import com.tickethub.infrastructure.operator.persistence.OperatorDocument;
import com.tickethub.infrastructure.operator.persistence.OperatorRepository;
import java.util.Optional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class OperatorMongoGateway implements OperatorGateway {

    private final MongoTemplate mongoTemplate;
    private final OperatorRepository repository;

    public OperatorMongoGateway(final MongoTemplate mongoTemplate, final OperatorRepository repository) {
        this.mongoTemplate = requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
        this.repository = requireNonNull(repository, "'repository' should not be null");
    }

    @Override
    public Operator create(final Operator operator) {
        return mongoTemplate
                .insert(OperatorDocument.from(operator), OperatorDocument.COLLECTION)
                .toDomain();
    }

    @Override
    public void deleteById(final OperatorID id) {
        repository.deleteById(id.getValue());
    }

    @Override
    public Optional<Operator> findById(final OperatorID id) {
        return repository.findById(id.getValue()).map(OperatorDocument::toDomain);
    }

    @Override
    public Optional<Operator> findByEmail(final Email email) {
        return repository.findByEmail(email.getValue()).map(OperatorDocument::toDomain);
    }

    @Override
    public Operator update(final Operator operator) {
        return mongoTemplate
                .save(OperatorDocument.from(operator), OperatorDocument.COLLECTION)
                .toDomain();
    }
}
