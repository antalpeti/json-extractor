package com.example.jsonextractor.controller;

import com.example.jsonextractor.model.ExtractionRequest;
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

    static final String FIELD_NAME = "name";
    static final String FIELD_VALUE = "value";
    static final String FIELD_LEFT = "left";
    static final String FIELD_RIGHT = "right";
    static final String FIELD_FIELD = "field";
    static final String FIELD_MISSING = "missing";
    static final String STRIP_CHARS = ",;:.!?-";

    static final String EXPECTED_TRANSFORM_BODY = "test1\tvalue2\n";
    static final String INVALID_JSON_ERROR_PREFIX = "Invalid JSON file:";
    static final String EMPTY_ROW_BODY = "\n";
    static final String CONTENT_DISPOSITION_PATTERN =
            "form-data; name=\"attachment\"; filename=\"extracted_\\d{8}_\\d{6}\\.txt\"";

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
}

