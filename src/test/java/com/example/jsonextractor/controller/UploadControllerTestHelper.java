package com.example.jsonextractor.controller;

import com.example.jsonextractor.model.ExtractionRequest;
import com.example.jsonextractor.model.FieldColumnEntry;
import com.example.jsonextractor.service.ExtractionTransformService;
import com.example.jsonextractor.service.JsonDataStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

class UploadControllerTestHelper {

    static final String ARRAY_JSON_ONE_RECORD = "[{\"name\":\"Test\",\"value\":\"1\"}]";
    static final String SINGLE_OBJECT_JSON = "{\"name\":\"Test\",\"value\":\"1\"}";
    static final String INVALID_JSON = "not valid json";
    static final String TRANSFORM_JSON = "[{\"left\":\"  Test1,. \",\"right\":\" Value2.,\"}]";
    static final String DOWNLOAD_JSON = "[{\"field\":\"data\"}]";
    static final String THREE_FIELD_JSON = "[{\"name\":\"Alice\",\"city\":\"London\",\"age\":\"30\"}]";

    static final String FIELD_NAME = "name";
    static final String FIELD_VALUE = "value";
    static final String FIELD_LEFT = "left";
    static final String FIELD_RIGHT = "right";
    static final String FIELD_FIELD = "field";
    static final String FIELD_MISSING = "missing";
    static final String FIELD_AGE = "age";
    static final String FIELD_CITY = "city";
    static final String STRIP_CHARS = ",;:.!?-";

    static final String EXPECTED_TRANSFORM_BODY = "test1\tvalue2\n";
    static final String INVALID_JSON_ERROR_PREFIX = "Invalid JSON file:";
    static final String EMPTY_ROW_BODY = "\n";
    static final String CONTENT_DISPOSITION_PATTERN =
            "form-data; name=\"attachment\"; filename=\"extracted_\\d{8}_\\d{6}\\.txt\"";

    // fieldColumns ordering: name(idx=1), age(idx=2), city(idx=3)
    static final String EXPECTED_FIELD_COLUMNS_ORDERED_BODY = "Alice\t30\tLondon\n";
    // fieldColumns precedence over legacy fields: age(idx=1), name(idx=2)
    static final String EXPECTED_FIELD_COLUMNS_PRECEDENCE_BODY = "30\tAlice\n";
    // null columnIndex last: city(idx=1), age(idx=2), name(idx=null → last)
    static final String EXPECTED_NULL_INDEX_LAST_BODY = "London\t30\tAlice\n";
    // blank/null field names skipped, only name(idx=3) survives
    static final String EXPECTED_BLANK_FIELD_SKIPPED_BODY = "Alice\n";

    JsonDataStore createJsonDataStore() {
        return new JsonDataStore();
    }

    UploadController createController(JsonDataStore store) {
        return new UploadController(store, new ObjectMapper(), new ExtractionTransformService());
    }

    MockMultipartFile createMultipartFile(String json) {
        return new MockMultipartFile(
                "file",
                "sample.json",
                "application/json",
                json.getBytes(StandardCharsets.UTF_8));
    }

    ExtractionRequest createRequestWithFields(List<String> fields) {
        final var request = new ExtractionRequest();
        request.setFields(fields);
        return request;
    }

    ExtractionRequest createTransformRequest() {
        final var request = new ExtractionRequest();
        request.setFields(List.of(FIELD_LEFT, FIELD_RIGHT));
        request.setTrimWhitespace(true);
        request.setLowercaseFirstLetter(true);
        request.setStripCharsEnabled(true);
        request.setStripChars(STRIP_CHARS);
        return request;
    }

    FieldColumnEntry createFieldColumnEntry(String field, Integer columnIndex) {
        final var entry = new FieldColumnEntry();
        entry.setField(field);
        entry.setColumnIndex(columnIndex);
        return entry;
    }

    ExtractionRequest createRequestWithFieldColumns(List<FieldColumnEntry> fieldColumns) {
        final var request = new ExtractionRequest();
        request.setFieldColumns(fieldColumns);
        return request;
    }
}

