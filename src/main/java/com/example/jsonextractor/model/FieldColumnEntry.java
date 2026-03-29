package com.example.jsonextractor.model;

/**
 * Represents a single field-to-column mapping in the extraction request.
 * {@code columnIndex} acts as a sort key: lower values appear earlier in
 * the tab-separated output. {@code null} is treated as {@link Integer#MAX_VALUE}
 * (appended last). Duplicate indices keep their original relative order
 * because {@link java.util.List#sort} is stable.
 */
public class FieldColumnEntry {

    private String field;
    private Integer columnIndex;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Integer getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(Integer columnIndex) {
        this.columnIndex = columnIndex;
    }
}

