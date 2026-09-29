package com.tickethub.application.ticket.retrieve.buyers;

public record ListShowBuyersCommand(String showId) {
    public static ListShowBuyersCommand with(final String showId) {
        return new ListShowBuyersCommand(showId);
    }
}
