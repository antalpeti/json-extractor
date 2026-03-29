package com.example.jsonextractor.model;

import java.util.List;

public class ExtractionRequest {

    /**
     * Legacy field list. Kept for backward compatibility with clients that
     * send {@code {"fields":["a","b",...]}}.  When {@link #fieldColumns} is
     * present and non-empty this list is ignored.
     */
    private List<String> fields;

    /**
     * New field-to-column mapping.  When non-null and non-empty the backend
     * sorts entries by {@link FieldColumnEntry#getColumnIndex()} (ascending,
     * {@code null} treated as {@link Integer#MAX_VALUE}) to determine the
     * output column order.  Entries with a blank field name are skipped.
     * Duplicate column indices keep their original relative order (stable sort).
     */
    private List<FieldColumnEntry> fieldColumns;
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

    public List<FieldColumnEntry> getFieldColumns() {
        return fieldColumns;
    }

    public void setFieldColumns(List<FieldColumnEntry> fieldColumns) {
        this.fieldColumns = fieldColumns;
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
