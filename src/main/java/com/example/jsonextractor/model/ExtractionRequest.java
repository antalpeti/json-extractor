package com.example.jsonextractor.model;

import java.util.List;

public class ExtractionRequest {

    private List<String> fields;
    private boolean lowercaseFirstLetter;
    private boolean trimWhitespace;
    private boolean removeTrailingDots;
    private boolean removeTrailingCommas;

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public boolean isLowercaseFirstLetter() {
        return lowercaseFirstLetter;
    }

    public void setLowercaseFirstLetter(boolean lowercaseFirstLetter) {
        this.lowercaseFirstLetter = lowercaseFirstLetter;
    }

    public boolean isTrimWhitespace() {
        return trimWhitespace;
    }

    public void setTrimWhitespace(boolean trimWhitespace) {
        this.trimWhitespace = trimWhitespace;
    }

    public boolean isRemoveTrailingDots() {
        return removeTrailingDots;
    }

    public void setRemoveTrailingDots(boolean removeTrailingDots) {
        this.removeTrailingDots = removeTrailingDots;
    }

    public boolean isRemoveTrailingCommas() {
        return removeTrailingCommas;
    }

    public void setRemoveTrailingCommas(boolean removeTrailingCommas) {
        this.removeTrailingCommas = removeTrailingCommas;
    }

}
