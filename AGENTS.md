# AGENTS.md

## Stack and runtime
- Spring Boot 3.4.3 on Java 21 (`pom.xml`); only `spring-boot-starter-web`, `thymeleaf`, and test dependencies are present.
- Default local URL is `http://localhost:8081/json-extractor/` because `server.port=8081` and `server.servlet.context-path=/json-extractor` are set in `src/main/resources/application.properties`.
- This is a server-rendered app with a single Thymeleaf page in `src/main/resources/templates/index.html`; there is no SPA build, database, JPA, or security layer.

## Big picture architecture
- `JsonExtractorApplication` boots a small MVC app; the main behavior lives in `UploadController`, `ExtractionTransformService`, and `JsonDataStore`.
- `UploadController` is both page controller and API surface: `GET /` returns `index`, `POST /upload` parses JSON and returns field names, `POST /extract` returns extracted plain text.
- `JsonDataStore` is an application-scoped in-memory store (`CopyOnWriteArrayList<JsonNode>`). The app is stateful: `/extract` uses whatever payload was most recently uploaded.
- `ExtractionTransformService` is pure transformation logic. It applies options in a fixed order: trim whitespace, lowercase first character, then strip configured characters from both ends.

## Request/data flow to preserve
- Frontend flow in `index.html`: select file -> `fetch('./upload')` -> render checkboxes from returned field names -> `fetch('./extract')` or `fetch('./extract?download=true')`.
- The relative `./upload` and `./extract` URLs are intentional so the UI keeps working under `/json-extractor`; avoid hard-coding `/upload` unless you also change the context-path behavior.
- `upload()` accepts either a single JSON object or a top-level array, but field names are derived only from the first record (`UploadController`, lines 50-65 behavior).
- `extract()` emits UTF-8 tab-separated rows with a trailing newline per record. Missing fields become empty strings.
- Download mode sets a `Content-Disposition` header via `setContentDispositionFormData("attachment", "extracted.txt")`; current tests assert the resulting header string exactly.

## Project-specific conventions
- Keep extraction behavior simple and text-based: no CSV quoting, no nested-path extraction, no schema inference beyond top-level keys.
- `ExtractionRequest` is a plain mutable POJO with booleans plus `fields`; there is no validation framework or Lombok.
- Tests treat controller methods as orchestration and the service as the transformation unit. Match that split when adding coverage.
- Because data lives in `JsonDataStore`, features that touch extraction usually need to consider upload-before-extract ordering and shared mutable state.

## Developer workflows
- Run the full test suite from repo root:
  ```bash
  mvn test
  ```
- Start the app locally:
  ```bash
  mvn spring-boot:run
  ```
- Open the UI at:
  ```text
  http://localhost:8081/json-extractor/
  ```

## Testing patterns in this repo
- Fast controller tests in `src/test/java/com/example/jsonextractor/controller/UploadControllerTest.java` instantiate `UploadController` directly with `new ObjectMapper()`, `new JsonDataStore()`, and `new ExtractionTransformService()`.
- Integration tests in `src/test/java/com/example/jsonextractor/UploadControllerIntegrationTest.java` use `@SpringBootTest` + `@AutoConfigureMockMvc`; requests include both the `/json-extractor` prefix and `.contextPath("/json-extractor")`.
- `ExtractionTransformServiceTest` uses a helper base class (`ExtractionTransformServiceHelper`) to build requests and shared sample strings; extend that style if you add more transformation cases.

## High-value gotchas for agents
- There is no persistence: uploading a new file replaces previous in-memory data for all later extraction calls.
- If you change response headers, context path handling, or transform order, update both unit and integration tests because those details are asserted explicitly.
- `README.md` is effectively empty, so prefer source files and tests as the source of truth.
