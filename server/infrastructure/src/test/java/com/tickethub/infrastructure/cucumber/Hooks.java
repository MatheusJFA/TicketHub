package com.tickethub.infrastructure.cucumber;

import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.audit.MongoAuditTrail;
import com.tickethub.infrastructure.customer.persistence.CustomerDocument;
import com.tickethub.infrastructure.partner.persistence.PartnerDocument;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.show.persistence.ShowDocument;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class Hooks {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private World world;

    @Before
    public void cleanUp() {
        MongoCleanUpExtension.cleanCollections(
                mongoTemplate,
                ShowDocument.COLLECTION,
                SectionDocument.COLLECTION,
                SpotDocument.COLLECTION,
                CustomerDocument.COLLECTION,
                PartnerDocument.COLLECTION,
                MongoAuditTrail.COLLECTION);
        world.reset();
    }
}
