package com.example.jsonextractor.controller;

import com.example.jsonextractor.model.ExtractionRequest;
import com.example.jsonextractor.service.ExtractionTransformService;
import com.example.jsonextractor.service.JsonDataStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UploadControllerTest {

    private UploadController controller;
    private JsonDataStore jsonDataStore;

    @BeforeEach
    void setUp() {
        jsonDataStore = new JsonDataStore();
        controller = new UploadController(jsonDataStore, new ObjectMapper(), new ExtractionTransformService());
    }

    @Test
    void uploadReturnsTopLevelFieldNamesAndStoresRecords() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.json",
                "application/json",
                "[{\"name\":\"Test\",\"value\":\"1\"}]".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<?> response = controller.upload(file);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(List.of("name", "value"));
        assertThat(jsonDataStore.hasData()).isTrue();
        assertThat(jsonDataStore.getData()).hasSize(1);
    }

    @Test
    void extractAppliesTransformOptionsAndReturnsTabSeparatedText() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.json",
                "application/json",
                "[{\"left\":\"  Test1,. \",\"right\":\" Value2.,\"}]".getBytes(StandardCharsets.UTF_8));
        controller.upload(file);

        ExtractionRequest request = new ExtractionRequest();
        request.setFields(List.of("left", "right"));
        request.setTrimWhitespace(true);
        request.setLowercaseFirstLetter(true);
        request.setRemoveTrailingDots(true);
        request.setRemoveTrailingCommas(true);

        ResponseEntity<byte[]> response = controller.extract(request, false);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(new String(response.getBody(), StandardCharsets.UTF_8)).isEqualTo("test1\tvalue2\n");
    }

    @Test
    void extractWithDownloadSetsAttachmentHeader() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.json",
                "application/json",
                "[{\"field\":\"data\"}]".getBytes(StandardCharsets.UTF_8));
        controller.upload(file);

        ExtractionRequest request = new ExtractionRequest();
        request.setFields(List.of("field"));

        ResponseEntity<byte[]> response = controller.extract(request, true);

        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("form-data; name=\"attachment\"; filename=\"extracted.txt\"");
    }
}


