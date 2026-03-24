package com.example.jsonextractor.service;

import com.example.jsonextractor.model.ExtractionRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractionTransformServiceTest {

    private final ExtractionTransformService service = new ExtractionTransformService();

    @Test
    void transformReturnsOriginalWhenNoOptionsEnabled() {
        ExtractionRequest request = new ExtractionRequest();

        String result = service.transform("Test Value.", request);

        assertThat(result).isEqualTo("Test Value.");
    }

    @Test
    void transformLowercasesFirstCharacterWhenEnabled() {
        ExtractionRequest request = new ExtractionRequest();
        request.setLowercaseFirstLetter(true);

        String result = service.transform("Test Tabulator Teszt", request);

        assertThat(result).isEqualTo("test Tabulator Teszt");
    }

    @Test
    void transformTrimsWhitespaceWhenEnabled() {
        ExtractionRequest request = new ExtractionRequest();
        request.setTrimWhitespace(true);

        String result = service.transform("   Test Tabulator Teszt   ", request);

        assertThat(result).isEqualTo("Test Tabulator Teszt");
    }

    @Test
    void transformRemovesTrailingDotsWhenEnabled() {
        ExtractionRequest request = new ExtractionRequest();
        request.setRemoveTrailingDots(true);

        assertThat(service.transform("Test1.", request)).isEqualTo("Test1");
        assertThat(service.transform("Test2..", request)).isEqualTo("Test2");
    }

    @Test
    void transformRemovesTrailingCommasWhenEnabled() {
        ExtractionRequest request = new ExtractionRequest();
        request.setRemoveTrailingCommas(true);

        assertThat(service.transform("Test1,", request)).isEqualTo("Test1");
        assertThat(service.transform("Test2,,", request)).isEqualTo("Test2");
    }

    @Test
    void transformRemovesMixedTrailingDotsAndCommasWhenBothEnabled() {
        ExtractionRequest request = new ExtractionRequest();
        request.setRemoveTrailingDots(true);
        request.setRemoveTrailingCommas(true);

        assertThat(service.transform("Test1,.", request)).isEqualTo("Test1");
        assertThat(service.transform("Test2.,", request)).isEqualTo("Test2");
        assertThat(service.transform("Test3,.,.,", request)).isEqualTo("Test3");
    }

    @Test
    void transformAppliesAllRulesInConfiguredOrder() {
        ExtractionRequest request = new ExtractionRequest();
        request.setTrimWhitespace(true);
        request.setLowercaseFirstLetter(true);
        request.setRemoveTrailingDots(true);
        request.setRemoveTrailingCommas(true);

        String result = service.transform("   Test1,.   ", request);

        assertThat(result).isEqualTo("test1");
    }
}

