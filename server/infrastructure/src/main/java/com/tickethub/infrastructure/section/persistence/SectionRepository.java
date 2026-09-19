package com.tickethub.infrastructure.section.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SectionRepository extends MongoRepository<SectionDocument, String> {

    List<SectionDocument> findByShowId(String showId);
}
