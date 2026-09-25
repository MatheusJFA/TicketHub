package com.tickethub.infrastructure.operator.persistence;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OperatorRepository extends MongoRepository<OperatorDocument, String> {

    Optional<OperatorDocument> findByEmail(String email);
}
