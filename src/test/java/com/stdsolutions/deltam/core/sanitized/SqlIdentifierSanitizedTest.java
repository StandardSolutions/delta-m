package com.stdsolutions.deltam.core.sanitized;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import com.stdsolutions.deltam.sanitized.SqlIdentifierSanitized;

import static org.junit.jupiter.api.Assertions.*;

class SqlIdentifierSanitizedTest {

    @Test
    void shouldReturnSameValueForValidIdentifier() {
        String input = "valid_table";
        SqlIdentifierSanitized identifier = new SqlIdentifierSanitized(input);

        assertEquals(input, identifier.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"table_name", "Table1", "t"})
    void shouldAcceptValidIdentifiers(String input) {
        SqlIdentifierSanitized identifier = new SqlIdentifierSanitized(input);
        assertEquals(input, identifier.value());
    }

    @Test
    void shouldTrimAndRemoveQuotes() {
        String input = "  \"'table_name'\"  ";
        SqlIdentifierSanitized identifier = new SqlIdentifierSanitized(input);

        assertEquals("table_name", identifier.value());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldRejectNullOrBlank(String input) {
        SqlIdentifierSanitized identifier = new SqlIdentifierSanitized(input);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                identifier::value
        );
        assertTrue(exception.getMessage().contains("cannot be null/empty"));
    }

    @Test
    void shouldRejectTooLongIdentifiers() {
        String input = "a".repeat(63) + "a"; // 64 символа
        SqlIdentifierSanitized identifier = new SqlIdentifierSanitized(input);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                identifier::value
        );
        assertTrue(exception.getMessage().contains("exceeds 63 characters"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123table", "table-name", "table name",
            "table$name", "таблица", "table;name"
    })
    void shouldRejectInvalidCharacters(String input) {
        SqlIdentifierSanitized identifier = new SqlIdentifierSanitized(input);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                identifier::value
        );
        assertTrue(exception.getMessage().contains("Invalid SQL identifier"));
    }

    @Test
    void toStringReturnsValueForValidIdentifier() {
        String input = "valid_table";
        SqlIdentifierSanitized identifier = new SqlIdentifierSanitized(input);

        assertEquals(input, identifier.toString());
    }

    @Test
    void toStringShowsInvalidMarkerForInvalidIdentifier() {
        String input = "invalid-table";
        SqlIdentifierSanitized identifier = new SqlIdentifierSanitized(input);

        assertEquals("[Invalid: " + input + "]", identifier.toString());
    }

    @Test
    void equalsAndHashCodeWorkCorrectly() {
        String value = "test_table";
        SqlIdentifierSanitized id1 = new SqlIdentifierSanitized(value);
        SqlIdentifierSanitized id2 = new SqlIdentifierSanitized(value);
        SqlIdentifierSanitized id3 = new SqlIdentifierSanitized("other_table");

        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.hashCode(), id3.hashCode());
    }

    @Test
    void shouldHandleMixedCaseIdentifiers() {
        String input = "TableName_123";
        SqlIdentifierSanitized identifier = new SqlIdentifierSanitized(input);

        assertEquals(input, identifier.value());
    }
}