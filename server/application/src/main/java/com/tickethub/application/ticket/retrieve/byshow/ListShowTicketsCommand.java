package com.tickethub.application.ticket.retrieve.byshow;

public record ListShowTicketsCommand(String showId) {
    public static ListShowTicketsCommand with(final String showId) {
        return new ListShowTicketsCommand(showId);
    }
}
