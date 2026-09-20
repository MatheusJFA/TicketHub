package com.tickethub.infrastructure.shared.presenters;

import static java.util.Objects.isNull;

import java.util.Currency;
import java.util.Optional;

import org.mapstruct.Mapper;

import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.Location;
import com.tickethub.domain.shared.Money;
import com.tickethub.infrastructure.api.models.AddressModel;
import com.tickethub.infrastructure.api.models.MoneyModel;

@Mapper(componentModel = "spring")
public interface SharedMapper {

    AddressModel toModel(Address address);

    default Address toDomain(final AddressModel model) {
        return Optional.ofNullable(model)
                .map(current -> Address.create(current.street(), current.number(),
                        current.complement(), current.neighborhood(), current.city(),
                        current.state(), current.country(), current.zipCode()))
                .orElse(null);
    }

    default MoneyModel toModel(final Money money) {
        return Optional.ofNullable(money)
                .map(current -> new MoneyModel(current.getValue(),
                        current.getCurrency().getCurrencyCode()))
                .orElse(null);
    }

    default Money toDomain(final MoneyModel model) {
        if (isNull(model)) {
            return null;
        }
        try {
            return Money.create(model.value(),
                    Optional.ofNullable(model.currency()).map(Currency::getInstance).orElse(null));
        } catch (final IllegalArgumentException exception) {
            throw new DomainException("Invalid currency");
        }
    }

    default String toValue(final Location location) {
        return Optional.ofNullable(location).map(Location::getValue).orElse(null);
    }

    default Location toLocation(final String value) {
        return Optional.ofNullable(value).map(Location::create).orElse(null);
    }
}
