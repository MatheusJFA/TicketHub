package com.tickethub.infrastructure.cache;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TTLs do cache de leitura. Catálogo (shows/sections) muda por ação do
 * parceiro e tolera minutos de staleness; disponibilidade de spots muda a
 * cada compra, então o TTL é de segundos e toda compra/cancelamento evicta.
 */
@ConfigurationProperties(prefix = "tickethub.cache")
public class TickethubCacheProperties {

    public static final String SHOWS = "shows";
    public static final String SECTIONS = "sections";
    public static final String SPOTS = "spots";

    private boolean enabled = true;
    private Duration showsTtl = Duration.ofMinutes(5);
    private Duration sectionsTtl = Duration.ofMinutes(5);
    private Duration spotsTtl = Duration.ofSeconds(15);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getShowsTtl() {
        return showsTtl;
    }

    public void setShowsTtl(final Duration showsTtl) {
        this.showsTtl = showsTtl;
    }

    public Duration getSectionsTtl() {
        return sectionsTtl;
    }

    public void setSectionsTtl(final Duration sectionsTtl) {
        this.sectionsTtl = sectionsTtl;
    }

    public Duration getSpotsTtl() {
        return spotsTtl;
    }

    public void setSpotsTtl(final Duration spotsTtl) {
        this.spotsTtl = spotsTtl;
    }
}
