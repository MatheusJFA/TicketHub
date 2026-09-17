package com.tickethub.infrastructure.configuration;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;

/**
 * Allows the application to boot (and expose Swagger/health) even when no
 * persistence adapters are configured. Every gateway operation fails with
 * 503 SERVICE_UNAVAILABLE, matching the documented contract.
 */
@Configuration(proxyBeanMethods = false)
public class PersistenceFallbackConfiguration {

    private static ResponseStatusException unavailable(String resource) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                resource + " persistence is not configured");
    }

    @Bean
    @ConditionalOnMissingBean(CustomerGateway.class)
    public CustomerGateway unavailableCustomerGateway() {
        return new CustomerGateway() {
            @Override public Customer create(Customer c) { throw unavailable("Customer"); }
            @Override public void deleteById(CustomerID id) { throw unavailable("Customer"); }
            @Override public Optional<Customer> findById(CustomerID id) { throw unavailable("Customer"); }
            @Override public Customer update(Customer c) { throw unavailable("Customer"); }
            @Override public Pagination<Customer> findAll(SearchQuery q) { throw unavailable("Customer"); }
        };
    }

    @Bean
    @ConditionalOnMissingBean(PartnerGateway.class)
    public PartnerGateway unavailablePartnerGateway() {
        return new PartnerGateway() {
            @Override public Partner create(Partner p) { throw unavailable("Partner"); }
            @Override public void deleteById(PartnerID id) { throw unavailable("Partner"); }
            @Override public Optional<Partner> findById(PartnerID id) { throw unavailable("Partner"); }
            @Override public Partner update(Partner p) { throw unavailable("Partner"); }
            @Override public Pagination<Partner> findAll(SearchQuery q) { throw unavailable("Partner"); }
        };
    }

    @Bean
    @ConditionalOnMissingBean(ShowGateway.class)
    public ShowGateway unavailableShowGateway() {
        return new ShowGateway() {
            @Override public Show create(Show s) { throw unavailable("Show"); }
            @Override public void deleteById(ShowID id) { throw unavailable("Show"); }
            @Override public Optional<Show> findById(ShowID id) { throw unavailable("Show"); }
            @Override public Show update(Show s) { throw unavailable("Show"); }
            @Override public Pagination<Show> findAll(SearchQuery q) { throw unavailable("Show"); }
        };
    }

    @Bean
    @ConditionalOnMissingBean(SectionGateway.class)
    public SectionGateway unavailableSectionGateway() {
        return new SectionGateway() {
            @Override public Section create(Section s) { throw unavailable("Section"); }
            @Override public void deleteById(SectionID id) { throw unavailable("Section"); }
            @Override public Optional<Section> findById(SectionID id) { throw unavailable("Section"); }
            @Override public Section update(Section s) { throw unavailable("Section"); }
            @Override public Pagination<Section> findAll(SearchQuery q) { throw unavailable("Section"); }
        };
    }

    @Bean
    @ConditionalOnMissingBean(SpotGateway.class)
    public SpotGateway unavailableSpotGateway() {
        return new SpotGateway() {
            @Override public Spot create(Spot s) { throw unavailable("Spot"); }
            @Override public void deleteById(SpotID id) { throw unavailable("Spot"); }
            @Override public Optional<Spot> findById(SpotID id) { throw unavailable("Spot"); }
            @Override public Spot update(Spot s) { throw unavailable("Spot"); }
            @Override public Pagination<Spot> findAll(SearchQuery q) { throw unavailable("Spot"); }
        };
    }
}
