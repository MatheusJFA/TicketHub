package com.tickethub.infrastructure.ticket;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.tickethub.domain.core.ticket.Ticket;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.core.ticket.TicketID;
import com.tickethub.infrastructure.ticket.persistence.TicketDocument;
import com.tickethub.infrastructure.ticket.persistence.TicketRepository;

@Component
public class TicketMongoGateway implements TicketGateway {

    private final MongoTemplate mongoTemplate;
    private final TicketRepository repository;

    public TicketMongoGateway(final MongoTemplate mongoTemplate, final TicketRepository repository) {
        this.mongoTemplate = requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
        this.repository = requireNonNull(repository, "'repository' should not be null");
    }

    @Override
    public Ticket create(final Ticket ticket) {
        return mongoTemplate.insert(TicketDocument.from(ticket), TicketDocument.COLLECTION).toDomain();
    }

    @Override
    public Optional<Ticket> findById(final TicketID id) {
        requireNonNull(id, "'id' should not be null");
        return repository.findById(id.getValue()).map(TicketDocument::toDomain);
    }

    @Override
    public Ticket update(final Ticket ticket) {
        return mongoTemplate.save(TicketDocument.from(ticket), TicketDocument.COLLECTION).toDomain();
    }
}
