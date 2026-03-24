package com.example.jsonextractor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UploadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadEndpointReturnsFieldNamesFromUploadedJson() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "records.json",
                MediaType.APPLICATION_JSON_VALUE,
                "[{\"name\":\"Alice\",\"city\":\"Budapest\"}]".getBytes());

        mockMvc.perform(multipart("/json-extractor/upload").file(file).contextPath("/json-extractor"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"name\",\"city\"]"));
    }

    @Test
    void extractEndpointAppliesConfiguredTransformations() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "records.json",
                MediaType.APPLICATION_JSON_VALUE,
                "[{\"first\":\"  Test1,. \",\"second\":\" Teszt2.,\"}]".getBytes());

        mockMvc.perform(multipart("/json-extractor/upload").file(file).contextPath("/json-extractor"))
                .andExpect(status().isOk());

        String body = """
                {
                  "fields": ["first", "second"],
                  "trimWhitespace": true,
                  "lowercaseFirstLetter": true,
                  "stripCharsEnabled": true,
                  "stripChars": ",;:.!?-"
                }
                """;

        mockMvc.perform(post("/json-extractor/extract").contextPath("/json-extractor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("test1\tteszt2\n"));
    }

    @Test
    void extractEndpointDownloadModeReturnsAttachmentHeaders() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "records.json",
                MediaType.APPLICATION_JSON_VALUE,
                "[{\"field\":\"Value\"}]".getBytes());

        mockMvc.perform(multipart("/json-extractor/upload").file(file).contextPath("/json-extractor"))
                .andExpect(status().isOk());

        String body = """
                {
                  "fields": ["field"]
                }
                """;

        mockMvc.perform(post("/json-extractor/extract?download=true").contextPath("/json-extractor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"extracted.txt\""));
    }
}



