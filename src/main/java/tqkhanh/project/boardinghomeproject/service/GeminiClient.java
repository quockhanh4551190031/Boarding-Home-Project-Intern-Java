package tqkhanh.project.boardinghomeproject.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiClient {
    private final RestClient restClient = RestClient.create();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-flash-latest}")
    private String model;

    /**
     *  @param systemInstruction hướng dẫn hệ thống (persona, FAQ context
     *  @param contents lịch sử hội thoại dạng [{role, parts:[{text}]}, ...], role là "user"
     *  hoặc "model" */

    public String generateReply(String systemInstruction, List<Map<String, Object>> contents) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model
                + ":generateContent?key="
                + apiKey;

        Map<String,Object> body = new LinkedHashMap<>();

        if (systemInstruction != null && !systemInstruction.isBlank()) {
            body.put("system_instruction", Map.of("parts", List.of(Map.of("text", systemInstruction))));
        }
        body.put("contents", contents);

        Map<?,?> response;
        try {
            response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            throw new IllegalStateException("Không kết nối được với GEMINI API: " + e.getMessage(), e);
        }

        try {
            List<?> candidates = (List<?>) response.get("candidates");
            Map<?,?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?,?> content = (Map<?, ?>) firstCandidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
            return (String) firstPart.get("text");
        } catch (Exception e) {
            throw new IllegalStateException("Phản hồi từ GEMINI API không đúng định dạng mong đợi: " + e.getMessage(), e);
        }
    }
}
