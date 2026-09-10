package com.tickethub.domain;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class EitherTest {

    @Test
    void leftFactoryCreatesLeft() {
        final Either<String, Integer> either = Either.left("err");

        assertTrue(either.isLeft());
        assertFalse(either.isRight());
        assertEquals("err", either.getLeft());
    }

    @Test
    void rightFactoryCreatesRight() {
        final Either<String, Integer> either = Either.right(42);

        assertTrue(either.isRight());
        assertFalse(either.isLeft());
        assertEquals(42, either.getRight());
    }

    @Test
    void getRightOnLeftThrows() {
        assertThrows(NoSuchElementException.class, () -> Either.left("err").getRight());
    }

    @Test
    void getLeftOnRightThrows() {
        assertThrows(NoSuchElementException.class, () -> Either.right(42).getLeft());
    }

    @Test
    void mapTransformsRight() {
        final Either<String, Integer> either = Either.right(21);

        assertEquals(42, either.map(v -> v * 2).getRight());
    }

    @Test
    void mapIsNoOpOnLeft() {
        final Either<String, Integer> either = Either.left("err");

        final var mapped = either.map(v -> v * 2);

        assertTrue(mapped.isLeft());
        assertEquals("err", mapped.getLeft());
    }

    @Test
    void flatMapChainsRight() {
        final Either<String, Integer> either = Either.right(21);

        assertEquals(42, either.flatMap(v -> Either.right(v * 2)).getRight());
    }

    @Test
    void flatMapPropagatesLeftFromFunction() {
        final Either<String, Integer> either = Either.right(21);

        final var result = either.flatMap(v -> Either.<String, Integer>left("bad"));

        assertEquals("bad", result.getLeft());
    }

    @Test
    void flatMapIsNoOpOnLeft() {
        final Either<String, Integer> either = Either.left("err");

        assertEquals("err", either.flatMap(v -> Either.right(v * 2)).getLeft());
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
