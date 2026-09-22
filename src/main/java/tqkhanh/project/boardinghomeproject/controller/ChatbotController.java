package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.service.ChatbotService;
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

    @PostMapping("/message")
    public ResponseEntity<ChatbotMessageResponse> sendMessage(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChatbotMessageRequest request) {
        return ResponseEntity.ok(chatbotService.chat(currentUser, request));
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatbotHistoryItem>> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatbotService.getHistory(sessionId));
    }
}