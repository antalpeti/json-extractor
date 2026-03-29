package com.example.jsonextractor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UploadController integration tests")
class UploadControllerIntegrationTest {

    private static final String THREE_FIELD_JSON =
            "[{\"name\":\"Alice\",\"city\":\"London\",\"age\":\"30\"}]";
    private static final String EXPECTED_FIELD_COLUMNS_ORDERED    = "Alice\t30\tLondon\n";
    private static final String EXPECTED_FIELD_COLUMNS_PRECEDENCE = "30\tAlice\n";
    private static final String EXPECTED_NULL_INDEX_LAST          = "London\t30\tAlice\n";
    private static final String EXPECTED_BLANK_FIELD_SKIPPED      = "Alice\n";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("upload endpoint returns field names from uploaded JSON array")
    void uploadEndpointReturnsFieldNamesFromUploadedJson() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "records.json",
                MediaType.APPLICATION_JSON_VALUE,
                "[{\"name\":\"Alice\",\"city\":\"Budapest\"}]".getBytes());

        mockMvc.perform(multipart("/json-extractor/upload").file(file).contextPath("/json-extractor"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"name\",\"city\"]"));
    }

    @Test
    @DisplayName("extract endpoint applies configured transformations")
    void extractEndpointAppliesConfiguredTransformations() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "records.json",
                MediaType.APPLICATION_JSON_VALUE,
                "[{\"first\":\"  Test1,. \",\"second\":\" Teszt2.,\"}]".getBytes());

        mockMvc.perform(multipart("/json-extractor/upload").file(file).contextPath("/json-extractor"))
                .andExpect(status().isOk());

        String body = """
                {
                  "fields": ["first", "second"],
                  "trimWhitespace": true,
                  "lowercaseFirstLetter": true,
                  "stripCharsEnabled": true,
                  "stripChars": ",;:.!?-"
                }
                """;

        mockMvc.perform(post("/json-extractor/extract").contextPath("/json-extractor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("test1\tteszt2\n"));
    }

    @Test
    @DisplayName("upload endpoint returns field names from single JSON object")
    void uploadEndpointReturnsSingleObjectFieldNames() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "record.json",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"Alice\",\"city\":\"Budapest\"}".getBytes());

        mockMvc.perform(multipart("/json-extractor/upload").file(file).contextPath("/json-extractor"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"name\",\"city\"]"));
    }

    @Test
    @DisplayName("upload endpoint returns 400 bad request for invalid JSON")
    void uploadEndpointInvalidJsonReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bad.json",
                MediaType.APPLICATION_JSON_VALUE,
                "not-valid-json".getBytes());

        mockMvc.perform(multipart("/json-extractor/upload").file(file).contextPath("/json-extractor"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(matchesPattern("(?s).*Invalid JSON file:.*")));
    }

    @Test
    @DisplayName("extract endpoint produces empty tab-separated value for missing field")
    void extractEndpointMissingFieldProducesEmptyValueInRow() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "records.json",
                MediaType.APPLICATION_JSON_VALUE,
                "[{\"name\":\"Alice\"}]".getBytes());

        mockMvc.perform(multipart("/json-extractor/upload").file(file).contextPath("/json-extractor"))
                .andExpect(status().isOk());

        String body = """
                {
                  "fields": ["name", "missing"]
                }
                """;

        mockMvc.perform(post("/json-extractor/extract").contextPath("/json-extractor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Alice\t\n"));
    }

    @Test
    @DisplayName("extract endpoint in download mode returns Content-Disposition attachment header")
    void extractEndpointDownloadModeReturnsAttachmentHeaders() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "records.json",
                MediaType.APPLICATION_JSON_VALUE,
                "[{\"field\":\"Value\"}]".getBytes());

        mockMvc.perform(multipart("/json-extractor/upload").file(file).contextPath("/json-extractor"))
                .andExpect(status().isOk());

        String body = """
                {
                  "fields": ["field"]
                }
                """;

        mockMvc.perform(post("/json-extractor/extract?download=true").contextPath("/json-extractor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        matchesPattern("form-data; name=\"attachment\"; filename=\"extracted_\\d{8}_\\d{6}\\.txt\"")));
    }

    @Test
    @DisplayName("extract with fieldColumns produces output ordered by columnIndex regardless of JSON field order")
    void testExtractWithFieldColumnsRespectsCustomColumnOrder() throws Exception {
        final var file = new MockMultipartFile(
                "file", "records.json", MediaType.APPLICATION_JSON_VALUE,
                THREE_FIELD_JSON.getBytes());
        mockMvc.perform(multipart("/json-extractor/upload").file(file).contextPath("/json-extractor"))
                .andExpect(status().isOk());

        final var body = """
                {
                  "fieldColumns": [
                    {"field": "name", "columnIndex": 1},
                    {"field": "age",  "columnIndex": 2},
                    {"field": "city", "columnIndex": 3}
                  ]
                }
                """;

        mockMvc.perform(post("/json-extractor/extract").contextPath("/json-extractor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string(EXPECTED_FIELD_COLUMNS_ORDERED));
    }

    @Test
    @DisplayName("extract with both fieldColumns and fields uses fieldColumns and ignores fields entirely")
    void testExtractFieldColumnsTakePrecedenceOverLegacyFields() throws Exception {
        final var file = new MockMultipartFile(
                "file", "records.json", MediaType.APPLICATION_JSON_VALUE,
                THREE_FIELD_JSON.getBytes());
        mockMvc.perform(multipart("/json-extractor/upload").file(file).contextPath("/json-extractor"))
                .andExpect(status().isOk());

        final var body = """
                {
                  "fieldColumns": [
                    {"field": "age",  "columnIndex": 1},
                    {"field": "name", "columnIndex": 2}
                  ],
                  "fields": ["city"]
                }
                """;

        mockMvc.perform(post("/json-extractor/extract").contextPath("/json-extractor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string(EXPECTED_FIELD_COLUMNS_PRECEDENCE));
    }

    @Test
    @DisplayName("extract with null columnIndex appends column last and blank field names are silently skipped")
    void testExtractNullColumnIndexSortsLastAndBlankFieldNamesAreSkipped() throws Exception {
        final var file = new MockMultipartFile(
                "file", "records.json", MediaType.APPLICATION_JSON_VALUE,
                THREE_FIELD_JSON.getBytes());
        mockMvc.perform(multipart("/json-extractor/upload").file(file).contextPath("/json-extractor"))
                .andExpect(status().isOk());

        final var nullIndexBody = """
                {
                  "fieldColumns": [
                    {"field": "city", "columnIndex": 1},
                    {"field": "age",  "columnIndex": 2},
                    {"field": "name", "columnIndex": null}
                  ]
                }
                """;

        mockMvc.perform(post("/json-extractor/extract").contextPath("/json-extractor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nullIndexBody))
                .andExpect(status().isOk())
                .andExpect(content().string(EXPECTED_NULL_INDEX_LAST));

        final var blankFieldBody = """
                {
                  "fieldColumns": [
                    {"field": "   ", "columnIndex": 1},
                    {"field": null,  "columnIndex": 2},
                    {"field": "name","columnIndex": 3}
                  ]
                }
                """;

        mockMvc.perform(post("/json-extractor/extract").contextPath("/json-extractor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blankFieldBody))
                .andExpect(status().isOk())
                .andExpect(content().string(EXPECTED_BLANK_FIELD_SKIPPED));
    }
}
