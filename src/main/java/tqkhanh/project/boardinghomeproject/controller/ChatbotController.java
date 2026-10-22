package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.service.ChatbotGuard;
import tqkhanh.project.boardinghomeproject.service.ChatbotService;
import tqkhanh.project.boardinghomeproject.util.ClientIp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatbotGuard chatbotGuard;

    @PostMapping("/message")
    public ResponseEntity<ChatbotMessageResponse> sendMessage(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChatbotMessageRequest request,
            HttpServletRequest http) {
        // Dữ liệu sai định dạng bị chặn ở @Valid nên không bị tính vào lượt dùng
        chatbotGuard.check(currentUser, ClientIp.of(http));
        return ResponseEntity.ok(chatbotService.chat(currentUser, request));
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatbotHistoryItem>> getHistory(@PathVariable String sessionId) {
        if (sessionId.length() > 36) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(chatbotService.getHistory(sessionId));
    }
}