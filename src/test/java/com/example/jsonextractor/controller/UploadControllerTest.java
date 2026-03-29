package com.example.jsonextractor.controller;

import com.example.jsonextractor.service.JsonDataStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UploadController")
class UploadControllerTest extends UploadControllerTestHelper {

    private final JsonDataStore jsonDataStore = createJsonDataStore();
    private final UploadController controller = createController(jsonDataStore);

    @Test
    @DisplayName("upload with JSON array stores all records and returns field names from first record")
    void testUploadArrayStoresRecordsAndReturnsFieldNames() {
        final var file = createMultipartFile(ARRAY_JSON_ONE_RECORD);

        final var response = controller.upload(file);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(List.of(FIELD_NAME, FIELD_VALUE), response.getBody());
        assertTrue(jsonDataStore.hasData());
        assertEquals(1, jsonDataStore.getData().size());
    }

    @Test
    @DisplayName("upload with single JSON object stores one record and returns field names from object")
    void testUploadSingleJsonObjectStoresOneRecordAndReturnsFieldNames() {
        final var file = createMultipartFile(SINGLE_OBJECT_JSON);

        final var response = controller.upload(file);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(List.of(FIELD_NAME, FIELD_VALUE), response.getBody());
        assertTrue(jsonDataStore.hasData());
        assertEquals(1, jsonDataStore.getData().size());
    }

    @Test
    @DisplayName("upload with invalid JSON returns bad request with error message starting with expected prefix")
    void testUploadInvalidJsonReturnsBadRequestWithExpectedMessagePrefix() {
        final var file = createMultipartFile(INVALID_JSON);

        final var response = controller.upload(file);

        assertTrue(response.getStatusCode().is4xxClientError());
        assertInstanceOf(String.class, response.getBody());
        assertTrue(((String) response.getBody()).startsWith(INVALID_JSON_ERROR_PREFIX));
    }

    @Test
    @DisplayName("extract with transform options returns tab-separated text with transformed values")
    void testExtractAppliesTransformOptionsAndReturnsTabSeparatedText() {
        controller.upload(createMultipartFile(TRANSFORM_JSON));
        final var request = createTransformRequest();

        final var response = controller.extract(request, false);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(EXPECTED_TRANSFORM_BODY, new String(response.getBody(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("extract with download false returns text/plain content type and empty string for missing fields")
    void testExtractWithDownloadFalseReturnsTextPlainAndEmptyStringForMissingFields() {
        controller.upload(createMultipartFile(ARRAY_JSON_ONE_RECORD));
        final var request = createRequestWithFields(List.of(FIELD_MISSING));

        final var response = controller.extract(request, false);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertEquals(EMPTY_ROW_BODY, new String(response.getBody(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("extract with empty data store returns empty payload")
    void testExtractWithEmptyDataStoreReturnsEmptyPayload() {
        final var request = createRequestWithFields(List.of(FIELD_NAME));

        final var response = controller.extract(request, false);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    @Test
    @DisplayName("extract with download true sets attachment Content-Disposition header")
    void testExtractWithDownloadSetsAttachmentHeader() {
        controller.upload(createMultipartFile(DOWNLOAD_JSON));
        final var request = createRequestWithFields(List.of(FIELD_FIELD));

        final var response = controller.extract(request, true);

        final var header = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(header);
        assertTrue(header.matches(CONTENT_DISPOSITION_PATTERN));
    }

    @Test
    @DisplayName("extract with fieldColumns having different columnIndex values produces output ordered by index ascending")
    void testExtractWithFieldColumnsSortsOutputByColumnIndexAscending() {
        controller.upload(createMultipartFile(THREE_FIELD_JSON));
        final var request = createRequestWithFieldColumns(List.of(
                createFieldColumnEntry(FIELD_CITY, 3),
                createFieldColumnEntry(FIELD_NAME, 1),
                createFieldColumnEntry(FIELD_AGE, 2)
        ));

        final var response = controller.extract(request, false);

        assertNotNull(response.getBody());
        assertEquals(EXPECTED_FIELD_COLUMNS_ORDERED_BODY, new String(response.getBody(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("extract with both fieldColumns and legacy fields present uses fieldColumns and ignores legacy fields")
    void testExtractWithFieldColumnsAndLegacyFieldsPreferFieldColumns() {
        controller.upload(createMultipartFile(THREE_FIELD_JSON));
        final var request = createRequestWithFieldColumns(List.of(
                createFieldColumnEntry(FIELD_AGE, 1),
                createFieldColumnEntry(FIELD_NAME, 2)
        ));
        request.setFields(List.of(FIELD_CITY, FIELD_NAME));

        final var response = controller.extract(request, false);

        assertNotNull(response.getBody());
        assertEquals(EXPECTED_FIELD_COLUMNS_PRECEDENCE_BODY, new String(response.getBody(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("extract with null columnIndex in fieldColumns places that entry deterministically last")
    void testExtractWithNullColumnIndexPlacesEntryLast() {
        controller.upload(createMultipartFile(THREE_FIELD_JSON));
        final var request = createRequestWithFieldColumns(List.of(
                createFieldColumnEntry(FIELD_NAME, null),
                createFieldColumnEntry(FIELD_CITY, 1),
                createFieldColumnEntry(FIELD_AGE, 2)
        ));

        final var response = controller.extract(request, false);

        assertNotNull(response.getBody());
        assertEquals(EXPECTED_NULL_INDEX_LAST_BODY, new String(response.getBody(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("extract with blank or null field name in fieldColumns skips those entries silently")
    void testExtractWithBlankOrNullFieldNameInFieldColumnsSkipsEntry() {
        controller.upload(createMultipartFile(THREE_FIELD_JSON));
        final var request = createRequestWithFieldColumns(List.of(
                createFieldColumnEntry("   ", 1),
                createFieldColumnEntry(null, 2),
                createFieldColumnEntry(FIELD_NAME, 3)
        ));

        final var response = controller.extract(request, false);

        assertNotNull(response.getBody());
        assertEquals(EXPECTED_BLANK_FIELD_SKIPPED_BODY, new String(response.getBody(), StandardCharsets.UTF_8));
    }
}
