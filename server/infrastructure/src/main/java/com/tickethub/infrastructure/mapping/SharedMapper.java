package com.tickethub.infrastructure.mapping;

import java.util.Currency;

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
        if (model == null) {
            return null;
        }
        return Address.create(model.street(), model.number(), model.complement(), model.neighborhood(),
                model.city(), model.state(), model.country(), model.zipCode());
    }

    default MoneyModel toModel(final Money money) {
        if (money == null) {
            return null;
        }
        return new MoneyModel(money.getValue(), money.getCurrency().getCurrencyCode());
    }

    default Money toDomain(final MoneyModel model) {
        if (model == null) {
            return null;
        }
        try {
            return Money.create(model.value(),
                    model.currency() == null ? null : Currency.getInstance(model.currency()));
        } catch (final IllegalArgumentException exception) {
            throw new DomainException("Invalid currency");
        }
    }

    default String toValue(final Location location) {
        return location == null ? null : location.getValue();
    }

    default Location toLocation(final String value) {
        return value == null ? null : Location.create(value);
    }
}
