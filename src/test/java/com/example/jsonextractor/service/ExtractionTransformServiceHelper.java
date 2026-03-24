package com.example.jsonextractor.service;

import com.example.jsonextractor.model.ExtractionRequest;

class ExtractionTransformServiceHelper {

    static final String STRIP_CHARS_PUNCTUATION = ",;:.!?-";
    static final String EMPTY_STRING = "";
    static final String CLEAN_VALUE = "Test1";
    static final String VALUE_WITH_TRAILING_STRIP_CHAR = "Test1.";
    static final String VALUE_WITH_LEADING_STRIP_CHAR = ".Test1";
    static final String VALUE_WITH_BOTH_ENDS_STRIP_CHARS = ",Test1,";
    static final String VALUE_WITH_MULTIPLE_CONSECUTIVE_STRIP_CHARS = ",,..Test1..,,";
    static final String VALUE_ENTIRELY_OF_STRIP_CHARS = ",,..";
    static final String VALUE_WITH_WHITESPACE = "   Test Value   ";
    static final String TRIMMED_VALUE = "Test Value";
    static final String UPPERCASE_FIRST_VALUE = "Hello World";
    static final String LOWERCASE_FIRST_VALUE = "hello World";
    static final String ALL_OPTIONS_INPUT = "   Test1,.   ";
    static final String ALL_OPTIONS_EXPECTED = "test1";

    ExtractionTransformService createService() {
        return new ExtractionTransformService();
    }

    ExtractionRequest createEmptyRequest() {
        return new ExtractionRequest();
    }

    ExtractionRequest createRequestWithTrimWhitespace() {
        final var request = new ExtractionRequest();
        request.setTrimWhitespace(true);
        return request;
    }

    ExtractionRequest createRequestWithLowercaseFirstLetter() {
        final var request = new ExtractionRequest();
        request.setLowercaseFirstLetter(true);
        return request;
    }

    ExtractionRequest createRequestWithStripCharsEnabled(String stripChars) {
        final var request = new ExtractionRequest();
        request.setStripCharsEnabled(true);
        request.setStripChars(stripChars);
        return request;
    }

    ExtractionRequest createRequestWithStripCharsDisabled(String stripChars) {
        final var request = new ExtractionRequest();
        request.setStripCharsEnabled(false);
        request.setStripChars(stripChars);
        return request;
    }

    ExtractionRequest createRequestWithAllOptions(String stripChars) {
        final var request = new ExtractionRequest();
        request.setTrimWhitespace(true);
        request.setLowercaseFirstLetter(true);
        request.setStripCharsEnabled(true);
        request.setStripChars(stripChars);
        return request;
    }
}

