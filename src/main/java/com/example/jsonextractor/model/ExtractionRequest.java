package com.example.jsonextractor.model;

import java.util.List;

public class ExtractionRequest {

    private List<String> fields;
    private boolean lowercaseFirstLetter;
    private boolean trimWhitespace;
    private boolean stripCharsEnabled;
    private String stripChars;

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

    public boolean isStripCharsEnabled() {
        return stripCharsEnabled;
    }

    public void setStripCharsEnabled(boolean stripCharsEnabled) {
        this.stripCharsEnabled = stripCharsEnabled;
    }

    public String getStripChars() {
        return stripChars;
    }

    public void setStripChars(String stripChars) {
        this.stripChars = stripChars;
    }

}
