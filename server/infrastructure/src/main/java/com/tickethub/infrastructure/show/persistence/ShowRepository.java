package com.tickethub.infrastructure.show.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShowRepository extends MongoRepository<ShowDocument, String> {}
