package com.example.jsonextractor.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JsonDataStore {

    private List<JsonNode> data = new ArrayList<>();

    public void setData(List<JsonNode> data) {
        this.data = data;
    }

    public List<JsonNode> getData() {
        return data;
    }

    public boolean hasData() {
        return !data.isEmpty();
    }

}
