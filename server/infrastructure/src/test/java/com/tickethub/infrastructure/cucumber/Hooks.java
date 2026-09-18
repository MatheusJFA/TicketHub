package com.tickethub.infrastructure.cucumber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.audit.MongoAuditTrail;
import com.tickethub.infrastructure.persistence.CustomerDocument;
import com.tickethub.infrastructure.persistence.PartnerDocument;
import com.tickethub.infrastructure.persistence.SectionDocument;
import com.tickethub.infrastructure.persistence.ShowDocument;
import com.tickethub.infrastructure.persistence.SpotDocument;

import io.cucumber.java.Before;

public class Hooks {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private World world;

    @Before
    public void cleanUp() {
        MongoCleanUpExtension.cleanCollections(mongoTemplate,
                ShowDocument.COLLECTION,
                SectionDocument.COLLECTION,
                SpotDocument.COLLECTION,
                CustomerDocument.COLLECTION,
                PartnerDocument.COLLECTION,
                MongoAuditTrail.COLLECTION);
        world.reset();
    }
}
