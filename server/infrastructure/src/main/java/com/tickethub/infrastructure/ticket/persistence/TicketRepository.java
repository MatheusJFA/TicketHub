package com.tickethub.infrastructure.ticket.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketRepository extends MongoRepository<TicketDocument, String> {
}
