package com.tickethub.infrastructure.spot.persistence;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpotRepository extends MongoRepository<SpotDocument, String> {

    List<SpotDocument> findByShowId(String showId);

    List<SpotDocument> findBySectionId(String sectionId);
}
