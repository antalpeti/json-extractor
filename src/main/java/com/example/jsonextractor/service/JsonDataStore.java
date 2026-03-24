package com.example.jsonextractor.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class JsonDataStore {

    private volatile List<JsonNode> data = new CopyOnWriteArrayList<>();

    public synchronized void setData(List<JsonNode> data) {
        this.data = new CopyOnWriteArrayList<>(data);
    }

    public List<JsonNode> getData() {
        return data;
    }

    public boolean hasData() {
        return !data.isEmpty();
    }

}
