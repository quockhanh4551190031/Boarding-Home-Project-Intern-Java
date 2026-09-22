package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.MessageResponse;
import tqkhanh.project.boardinghomeproject.dto.SendMessageRequest;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(SendMessageRequest request, Authentication authentication) {
        User sender = (User) authentication.getPrincipal();

        MessageResponse response = chatService.sendMessage(sender, request);

        // Phát tin nhắn tới mọi client đang subscribe phòng chat này
        messagingTemplate.convertAndSend("/topic/chat/" + request.chatRoomId(), response);
    }
}