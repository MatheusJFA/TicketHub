package com.tickethub.infrastructure.partner.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PartnerRepository extends MongoRepository<PartnerDocument, String> {
}
