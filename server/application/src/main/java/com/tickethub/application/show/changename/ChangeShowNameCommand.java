package com.tickethub.application.show.changename;

public record ChangeShowNameCommand(String id, String name) {
    public static ChangeShowNameCommand with(final String id, final String name) {
        return new ChangeShowNameCommand(id, name);
    }
}
