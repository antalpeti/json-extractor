package com.example.jsonextractor.service;

import com.example.jsonextractor.model.ExtractionRequest;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ExtractionTransformService {

    public String transform(String value, ExtractionRequest request) {
        String transformed = value == null ? "" : value;

        if (request != null && request.isTrimWhitespace()) {
            transformed = transformed.trim();
        }

        if (request != null && request.isLowercaseFirstLetter() && !transformed.isEmpty()) {
            transformed = transformed.substring(0, 1).toLowerCase(Locale.ROOT) + transformed.substring(1);
        }

        if (request != null && request.isStripCharsEnabled()
                && request.getStripChars() != null && !request.getStripChars().isEmpty()) {
            transformed = stripBothEnds(transformed, request.getStripChars());
        }

        return transformed;
    }

    private String stripBothEnds(String value, String stripChars) {
        int start = 0;
        int end = value.length();

        while (start < end && stripChars.indexOf(value.charAt(start)) >= 0) {
            start++;
        }

        while (end > start && stripChars.indexOf(value.charAt(end - 1)) >= 0) {
            end--;
        }

        return value.substring(start, end);
    }
}

