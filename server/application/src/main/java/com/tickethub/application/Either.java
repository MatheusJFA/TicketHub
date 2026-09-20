package com.tickethub.application;

import static java.util.Objects.requireNonNull;
import java.util.NoSuchElementException;
import java.util.function.Function;

public sealed interface Either<L, R>
        permits Either.Left, Either.Right {

    record Left<L, R>(L value) implements Either<L, R> {
        public Left {
            requireNonNull(value, "Left value must not be null");
        }
    }

    record Right<L, R>(R value) implements Either<L, R> {
        public Right {
            requireNonNull(value, "Right value must not be null");
        }
    }

    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    default boolean isLeft() {
        return this instanceof Left<?, ?>;
    }

    default boolean isRight() {
        return this instanceof Right<?, ?>;
    }

    default L getLeft() {
        return leftOrThrow();
    }

    default R getRight() {
        return rightOrThrow();
    }

    default L leftOrThrow() {
        return switch (this) {
            case Left<L, R>(var value) -> value;
            case Right<L, R> ignored ->
                    throw new NoSuchElementException("Expected Left, but found Right");
        };
    }

    default R rightOrThrow() {
        return switch (this) {
            case Left<L, R> ignored ->
                    throw new NoSuchElementException("Expected Right, but found Left");
            case Right<L, R>(var value) -> value;
        };
    }

    default <T> Either<L, T> map(
            Function<? super R, ? extends T> mapper
    ) {
        requireNonNull(mapper, "mapper");

        return switch (this) {
            case Left<L, R>(var value) -> Either.left(value);
            case Right<L, R>(var value) -> Either.right(mapper.apply(value));
        };
    }

    default <T> Either<L, T> flatMap(
            Function<? super R, ? extends Either<L, T>> mapper
    ) {
        requireNonNull(mapper, "mapper");

        return switch (this) {
            case Left<L, R>(var value) -> Either.left(value);
            case Right<L, R>(var value) -> mapper.apply(value);
        };
    }

    default <T> T fold(
            Function<? super L, ? extends T> onLeft,
            Function<? super R, ? extends T> onRight
    ) {
        requireNonNull(onLeft, "onLeft");
        requireNonNull(onRight, "onRight");

        return switch (this) {
            case Left<L, R>(var value) -> onLeft.apply(value);
            case Right<L, R>(var value) -> onRight.apply(value);
        };
    }
}
