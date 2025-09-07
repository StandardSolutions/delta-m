package com.stdsolutions.deltam.options;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import com.stdsolutions.deltam.options.Options;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class OptionsTest {

    // Test subclass since Options is abstract
    private static class TestOptions extends Options {
        TestOptions(String... args) {
            super(args);
        }

        TestOptions(Iterable<String> args) {
            super(args);
        }

        Map<String, String> options() {
            return this.map;
        }
    }

    @Test
    void constructor_withNullArray_createsEmptyMap() {
        TestOptions options = new TestOptions((String[]) null);
        assertTrue(options.options().isEmpty());
    }

    @Test
    void constructor_withNullIterable_createsEmptyMap() {
        TestOptions options = new TestOptions((Iterable<String>) null);
        assertTrue(options.options().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void asMap_ignoresNullOrBlankArguments(String arg) {
        TestOptions options = new TestOptions(arg, "--valid=test");
        assertEquals(1, options.options().size());
        assertEquals("test", options.options().get("valid"));
    }

    @Test
    void asMap_handlesEmptyArguments() {
        TestOptions options = new TestOptions();
        assertTrue(options.options().isEmpty());
    }

    @Test
    void asMap_parsesMixedArguments() {
        TestOptions options = new TestOptions("--name=test", "--verbose", "--test-one=high", "--test_Two=low");
        Map<String, String> expected = Map.of(
                "name", "test",
                "verbose", "",
                "test-one", "high",
                "test_two", "low"
        );
        assertEquals(expected, options.options());
    }

    @Test
    void asMap_handlesListOfArguments() {
        TestOptions options = new TestOptions(List.of("--name=test", "--verbose"));
        Map<String, String> expected = Map.of(
                "name", "test",
                "verbose", ""
        );
        assertEquals(expected, options.options());
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentsProvider")
    void asMap_throwsExceptionForInvalidArguments(String invalidArg, String expectedMessage) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TestOptions(invalidArg)
        );
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    private static Stream<Arguments> invalidArgumentsProvider() {
        return Stream.of(
                Arguments.of("-name=value", "Invalid argument format"),
                Arguments.of("--=value", "Invalid argument format"),
                Arguments.of("--123=value", "Invalid argument format"),
                Arguments.of("--name value", "Invalid argument format"),
                Arguments.of("name=value", "Invalid argument format"),
                Arguments.of("--name:value", "Invalid argument format")
        );
    }

    @Test
    void asMap_handlesEmptyValues() {
        TestOptions options = new TestOptions("--empty=", "--not-empty=value");
        Map<String, String> expected = Map.of(
                "empty", "",
                "not-empty", "value"
        );
        assertEquals(expected, options.options());
    }

    @Test
    void asMap_handlesDuplicateArguments_lastOneWins() {
        TestOptions options = new TestOptions("--name=first", "--name=last");
        assertEquals(1, options.options().size());
        assertEquals("last", options.options().get("name"));
    }
}