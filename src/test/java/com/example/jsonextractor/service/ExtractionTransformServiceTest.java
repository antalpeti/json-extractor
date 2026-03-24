package com.example.jsonextractor.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExtractionTransformService")
class ExtractionTransformServiceTest extends ExtractionTransformServiceHelper {

    private final ExtractionTransformService service = createService();

    @Test
    @DisplayName("transform returns empty string when value is null")
    void testTransformReturnsEmptyStringWhenValueIsNull() {
        final var request = createEmptyRequest();

        final var result = service.transform(null, request);

        assertEquals(EMPTY_STRING, result);
    }

    @Test
    @DisplayName("transform returns value unchanged when request is null")
    void testTransformReturnsValueUnchangedWhenRequestIsNull() {
        final var result = service.transform(CLEAN_VALUE, null);

        assertEquals(CLEAN_VALUE, result);
    }

    @Test
    @DisplayName("transform returns value unchanged when no options are enabled")
    void testTransformReturnsValueUnchangedWhenNoOptionsEnabled() {
        final var request = createEmptyRequest();

        final var result = service.transform(VALUE_WITH_TRAILING_STRIP_CHAR, request);

        assertEquals(VALUE_WITH_TRAILING_STRIP_CHAR, result);
    }

    @Test
    @DisplayName("transform trims leading and trailing whitespace when trimWhitespace is enabled")
    void testTransformTrimsLeadingAndTrailingWhitespaceWhenTrimEnabled() {
        final var request = createRequestWithTrimWhitespace();

        final var result = service.transform(VALUE_WITH_WHITESPACE, request);

        assertEquals(TRIMMED_VALUE, result);
    }

    @Test
    @DisplayName("transform lowercases first letter when lowercaseFirstLetter is enabled")
    void testTransformLowercasesFirstLetterWhenEnabled() {
        final var request = createRequestWithLowercaseFirstLetter();

        final var result = service.transform(UPPERCASE_FIRST_VALUE, request);

        assertEquals(LOWERCASE_FIRST_VALUE, result);
    }

    @Test
    @DisplayName("transform skips lowercasing when value is empty and lowercaseFirstLetter is enabled")
    void testTransformSkipsLowercasingWhenValueIsEmptyAndLowercaseEnabled() {
        final var request = createRequestWithLowercaseFirstLetter();

        final var result = service.transform(EMPTY_STRING, request);

        assertEquals(EMPTY_STRING, result);
    }

    @Test
    @DisplayName("transform strips trailing strip characters when stripChars is enabled")
    void testTransformStripsTrailingStripCharsWhenEnabled() {
        final var request = createRequestWithStripCharsEnabled(STRIP_CHARS_PUNCTUATION);

        final var result = service.transform(VALUE_WITH_TRAILING_STRIP_CHAR, request);

        assertEquals(CLEAN_VALUE, result);
    }

    @Test
    @DisplayName("transform strips leading strip characters when stripChars is enabled")
    void testTransformStripsLeadingStripCharsWhenEnabled() {
        final var request = createRequestWithStripCharsEnabled(STRIP_CHARS_PUNCTUATION);

        final var result = service.transform(VALUE_WITH_LEADING_STRIP_CHAR, request);

        assertEquals(CLEAN_VALUE, result);
    }

    @Test
    @DisplayName("transform strips characters from both ends when stripChars is enabled")
    void testTransformStripsCharactersFromBothEndsWhenEnabled() {
        final var request = createRequestWithStripCharsEnabled(STRIP_CHARS_PUNCTUATION);

        final var result = service.transform(VALUE_WITH_BOTH_ENDS_STRIP_CHARS, request);

        assertEquals(CLEAN_VALUE, result);
    }

    @Test
    @DisplayName("transform strips multiple consecutive strip characters from both ends when stripChars is enabled")
    void testTransformStripsMultipleConsecutiveStripCharsFromBothEndsWhenEnabled() {
        final var request = createRequestWithStripCharsEnabled(STRIP_CHARS_PUNCTUATION);

        final var result = service.transform(VALUE_WITH_MULTIPLE_CONSECUTIVE_STRIP_CHARS, request);

        assertEquals(CLEAN_VALUE, result);
    }

    @Test
    @DisplayName("transform returns empty string when all characters match strip characters")
    void testTransformReturnsEmptyStringWhenAllCharsMatchStripChars() {
        final var request = createRequestWithStripCharsEnabled(STRIP_CHARS_PUNCTUATION);

        final var result = service.transform(VALUE_ENTIRELY_OF_STRIP_CHARS, request);

        assertEquals(EMPTY_STRING, result);
    }

    @Test
    @DisplayName("transform does not strip when stripCharsEnabled is false")
    void testTransformDoesNotStripWhenStripCharsDisabled() {
        final var request = createRequestWithStripCharsDisabled(STRIP_CHARS_PUNCTUATION);

        final var result = service.transform(VALUE_WITH_TRAILING_STRIP_CHAR, request);

        assertEquals(VALUE_WITH_TRAILING_STRIP_CHAR, result);
    }

    @Test
    @DisplayName("transform does not strip when stripChars is null")
    void testTransformDoesNotStripWhenStripCharsIsNull() {
        final var request = createRequestWithStripCharsEnabled(null);

        final var result = service.transform(VALUE_WITH_TRAILING_STRIP_CHAR, request);

        assertEquals(VALUE_WITH_TRAILING_STRIP_CHAR, result);
    }

    @Test
    @DisplayName("transform does not strip when stripChars is empty string")
    void testTransformDoesNotStripWhenStripCharsIsEmpty() {
        final var request = createRequestWithStripCharsEnabled(EMPTY_STRING);

        final var result = service.transform(VALUE_WITH_TRAILING_STRIP_CHAR, request);

        assertEquals(VALUE_WITH_TRAILING_STRIP_CHAR, result);
    }

    @Test
    @DisplayName("transform applies trim, lowercase, and strip in order when all options are enabled")
    void testTransformAppliesAllOptionsInOrder() {
        final var request = createRequestWithAllOptions(STRIP_CHARS_PUNCTUATION);

        final var result = service.transform(ALL_OPTIONS_INPUT, request);

        assertEquals(ALL_OPTIONS_EXPECTED, result);
    }
}
