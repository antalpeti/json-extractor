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

        if (request != null && (request.isRemoveTrailingDots() || request.isRemoveTrailingCommas())) {
            transformed = stripTrailingPunctuation(
                    transformed,
                    request.isRemoveTrailingDots(),
                    request.isRemoveTrailingCommas());
        }

        return transformed;
    }

    private String stripTrailingPunctuation(String value, boolean removeDots, boolean removeCommas) {
        int end = value.length();

        while (end > 0) {
            char last = value.charAt(end - 1);
            boolean shouldStripDot = removeDots && last == '.';
            boolean shouldStripComma = removeCommas && last == ',';

            if (!shouldStripDot && !shouldStripComma) {
                break;
            }

            end--;
        }

        return value.substring(0, end);
    }
}

