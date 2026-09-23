package com.tickethub.infrastructure.partner.persistence;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PartnerRepository extends MongoRepository<PartnerDocument, String> {

    Optional<PartnerDocument> findByEmail(String email);
}
