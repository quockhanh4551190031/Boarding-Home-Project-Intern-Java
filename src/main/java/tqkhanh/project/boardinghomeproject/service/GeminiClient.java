package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.exception.GeminiOverloadedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiClient {

    private final RestClient restClient = RestClient.create();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String model;

    private static final int MAX_RETRIES = 2;
    private static final long[] RETRY_DELAYS_MS = {1000, 2000};

    public String generateReply(String systemInstruction, List<Map<String, Object>> contents) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        Map<String, Object> body = new LinkedHashMap<>();
        if (systemInstruction != null && !systemInstruction.isBlank()) {
            body.put("system_instruction", Map.of("parts", List.of(Map.of("text", systemInstruction))));
        }
        body.put("contents", contents);

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                Map<?, ?> response = restClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(Map.class);

                return extractText(response);

            } catch (RestClientResponseException e) {
                String rawBody = e.getResponseBodyAsString().toLowerCase();
                boolean isOverloaded = e.getStatusCode().value() == 503
                        || rawBody.contains("overloaded")
                        || rawBody.contains("unavailable");

                if (isOverloaded && attempt < MAX_RETRIES) {
                    sleep(RETRY_DELAYS_MS[attempt]);
                    continue; // thử lại lần tiếp theo
                }
                if (isOverloaded) {
                    throw new GeminiOverloadedException(
                            "Hệ thống chatbot đang quá tải, vui lòng thử lại sau ít phút");
                }
                throw new IllegalStateException("Lỗi từ Gemini API: " + e.getMessage(), e);

            } catch (Exception e) {
                throw new IllegalStateException("Không thể kết nối tới Gemini API: " + e.getMessage(), e);
            }
        }

        throw new GeminiOverloadedException("Hệ thống chatbot đang quá tải, vui lòng thử lại sau ít phút");
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private String extractText(Map<?, ?> response) {
        try {
            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
            return (String) firstPart.get("text");
        } catch (Exception e) {
            throw new IllegalStateException("Phản hồi từ Gemini API không đúng định dạng mong đợi");
        }
    }
}