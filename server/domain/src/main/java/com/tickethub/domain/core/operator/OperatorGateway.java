package com.tickethub.domain.core.operator;

import com.tickethub.domain.shared.Email;
import java.util.Optional;

public interface OperatorGateway {
    Operator create(Operator operator);

    void deleteById(OperatorID id);

    Optional<Operator> findById(OperatorID id);

    Optional<Operator> findByEmail(Email email);

    Operator update(Operator operator);
}
