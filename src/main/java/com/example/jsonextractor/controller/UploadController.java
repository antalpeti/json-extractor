package com.example.jsonextractor.controller;

import com.example.jsonextractor.model.ExtractionRequest;
import com.example.jsonextractor.model.FieldColumnEntry;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class UploadController {

    private static final DateTimeFormatter FILENAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

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
        List<String> fields = resolveOrderedFields(request);

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
            final var filename = "extracted_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".txt";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", filename);
            return ResponseEntity.ok().headers(headers).body(content);
        } else {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(content);
        }
    }

    /**
     * Determines the ordered field list from an {@link ExtractionRequest}.
     *
     * <p>New format: when {@code fieldColumns} is present and non-empty the
     * entries are sorted by {@code columnIndex} (ascending; {@code null}
     * sorts last). Entries with a {@code null} or blank field name are
     * silently skipped. Duplicate column indices keep their original relative
     * order because {@link List#sort} uses a stable algorithm.
     *
     * <p>Legacy format: falls back to the plain {@code fields} list unchanged.
     * If both are absent/empty an empty list is returned so the output
     * contains zero columns (valid: each row is just a bare newline).
     */
    private List<String> resolveOrderedFields(ExtractionRequest request) {
        if (request == null) {
            return List.of();
        }

        List<FieldColumnEntry> fieldColumns = request.getFieldColumns();
        if (fieldColumns != null && !fieldColumns.isEmpty()) {
            return fieldColumns.stream()
                    .filter(e -> e != null && e.getField() != null && !e.getField().isBlank())
                    .sorted(Comparator.comparingInt(
                            e -> isValidColumnIndex(e.getColumnIndex()) ? e.getColumnIndex() : Integer.MAX_VALUE))
                    .map(FieldColumnEntry::getField)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(LinkedHashSet::new),
                            ArrayList::new));
        }
        List<String> fields = request.getFields();
        return fields != null ? fields : List.of();
    }

    private boolean isValidColumnIndex(Integer columnIndex) {
        return columnIndex != null && columnIndex > 0;
    }

}
