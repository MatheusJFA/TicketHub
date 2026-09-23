package com.tickethub.infrastructure.customer.persistence;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<CustomerDocument, String> {

    Optional<CustomerDocument> findByEmail(String email);
}
