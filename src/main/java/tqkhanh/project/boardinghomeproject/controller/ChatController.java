package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.service.ChatService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getMyRooms(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(chatService.getMyChatRooms(currentUser));
    }

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createOrGetRoom(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateChatRoomRequest request) {
        return ResponseEntity.ok(chatService.getOrCreateChatRoom(currentUser, request));
    }

    @GetMapping("/rooms/{id}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatService.getMessages(currentUser, id, page, size));
    }

    @PutMapping("/rooms/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        chatService.markAsRead(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}