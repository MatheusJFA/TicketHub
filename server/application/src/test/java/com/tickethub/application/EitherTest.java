package com.tickethub.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class EitherTest {

    @Test
    void leftFactoryCreatesLeft() {
        final Either<String, Integer> either = Either.left("err");

        assertTrue(either.isLeft());
        assertFalse(either.isRight());
        assertEquals("L:err", either.fold(l -> "L:" + l, r -> "R:" + r));
    }

    @Test
    void rightFactoryCreatesRight() {
        final Either<String, Integer> either = Either.right(42);

        assertTrue(either.isRight());
        assertFalse(either.isLeft());
        assertEquals("R:42", either.fold(l -> "L:" + l, r -> "R:" + r));
    }

    @Test
    void mapTransformsRight() {
        final Either<String, Integer> either = Either.right(21);

        assertEquals("R:42", either.map(v -> v * 2).fold(l -> "L:" + l, r -> "R:" + r));
    }

    @Test
    void mapIsNoOpOnLeft() {
        final Either<String, Integer> either = Either.left("err");

        final var mapped = either.map(v -> v * 2);

        assertTrue(mapped.isLeft());
        assertEquals("L:err", mapped.fold(l -> "L:" + l, r -> "R:" + r));
    }

    @Test
    void flatMapChainsRight() {
        final Either<String, Integer> either = Either.right(21);

        assertEquals("R:42", either.flatMap(v -> Either.right(v * 2))
            .fold(l -> "L:" + l, r -> "R:" + r));
    }

    @Test
    void flatMapPropagatesLeftFromFunction() {
        final Either<String, Integer> either = Either.right(21);

        final var result = either.flatMap(v -> Either.<String, Integer>left("bad"));

        assertEquals("L:bad", result.fold(l -> "L:" + l, r -> "R:" + r));
    }

    @Test
    void flatMapIsNoOpOnLeft() {
        final Either<String, Integer> either = Either.left("err");

        assertEquals("L:err", either.flatMap(v -> Either.right(v * 2))
            .fold(l -> "L:" + l, r -> "R:" + r));
    }

    @Test
    void foldAppliesTheMatchingSide() {
        assertEquals("L:err", Either.<String, Integer>left("err").fold(l -> "L:" + l, r -> "R:" + r));
        assertEquals("R:42", Either.<String, Integer>right(42).fold(l -> "L:" + l, r -> "R:" + r));
    }

    @Test
    void supportsExhaustivePatternMatching() {
        final Either<String, Integer> either = Either.right(42);

        final var description = switch (either) {
            case Either.Left<String, Integer>(var l) -> "left " + l;
            case Either.Right<String, Integer>(var r) -> "right " + r;
        };

        assertEquals("right 42", description);
    }
}
