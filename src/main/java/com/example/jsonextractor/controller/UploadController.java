package com.example.jsonextractor.controller;

import com.example.jsonextractor.model.ExtractionRequest;
import com.example.jsonextractor.service.ExtractionTransformService;
import com.example.jsonextractor.service.JsonDataStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
public class UploadController {

    private final JsonDataStore jsonDataStore;
    private final ObjectMapper objectMapper;
    private final ExtractionTransformService extractionTransformService;

    public UploadController(
            JsonDataStore jsonDataStore,
            ObjectMapper objectMapper,
            ExtractionTransformService extractionTransformService) {
        this.jsonDataStore = jsonDataStore;
        this.objectMapper = objectMapper;
        this.extractionTransformService = extractionTransformService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            JsonNode rootNode = objectMapper.readTree(file.getInputStream());

            List<JsonNode> records = new ArrayList<>();
            if (rootNode.isArray()) {
                rootNode.forEach(records::add);
            } else {
                records.add(rootNode);
            }

            jsonDataStore.setData(records);

            List<String> fieldNames = new ArrayList<>();
            if (!records.isEmpty()) {
                Iterator<Map.Entry<String, JsonNode>> fieldEntries = records.get(0).fields();
                while (fieldEntries.hasNext()) {
                    fieldNames.add(fieldEntries.next().getKey());
                }
            }

            return ResponseEntity.ok(fieldNames);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Invalid JSON file: " + e.getMessage());
        }
    }

    @PostMapping("/extract")
    public ResponseEntity<byte[]> extract(
            @RequestBody ExtractionRequest request,
            @RequestParam(value = "download", defaultValue = "false") boolean download) {

        List<JsonNode> data = jsonDataStore.getData();
        List<String> fields = request.getFields();

        StringBuilder sb = new StringBuilder();
        for (JsonNode record : data) {
            List<String> values = new ArrayList<>();
            for (String field : fields) {
                JsonNode valueNode = record.get(field);
                String rawValue = valueNode != null ? valueNode.asText() : "";
                values.add(extractionTransformService.transform(rawValue, request));
            }
            sb.append(String.join("\t", values)).append("\n");
        }

        byte[] content = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        if (download) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "extracted.txt");
            return ResponseEntity.ok().headers(headers).body(content);
        } else {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(content);
        }
    }

}
