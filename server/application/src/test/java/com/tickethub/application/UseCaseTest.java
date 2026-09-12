package com.tickethub.application;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class UseCaseTest {
    protected abstract List<Object> getMocks();

    @AfterEach
    void verifyNoUnexpectedInteractions() {
        Mockito.verifyNoMoreInteractions(getMocks().toArray());
    }
}