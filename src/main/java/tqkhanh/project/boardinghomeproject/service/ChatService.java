package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.*;
import tqkhanh.project.boardinghomeproject.exception.ForbiddenActionException;
import tqkhanh.project.boardinghomeproject.exception.ResourceNotFoundException;
import tqkhanh.project.boardinghomeproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tqkhanh.project.boardinghomeproject.util.DisplayNames;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Tin nhắn tự động (ví dụ lịch xem phòng): lưu vào cuộc trò chuyện giữa hai người, phát realtime sau khi commit.
     */
    @Transactional
    public void postSystemMessage(User sender, User receiver, Room relatedRoom, String content) {
        ChatRoom chatRoom = chatRoomRepository.findBetweenUsers(sender.getId(), receiver.getId())
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder()
                        .user1(sender).user2(receiver).relatedRoom(relatedRoom).build()));

        Message message = messageRepository.save(Message.builder()
                .chatRoom(chatRoom).sender(sender).content(content).messageType(MessageType.TEXT).build());

        MessageResponse response = toMessageResponse(message);
        String destination = "/topic/chat/" + chatRoom.getId();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend(destination, response);
                }
            });
        } else {
            messagingTemplate.convertAndSend(destination, response);
        }
    }

    @Transactional(readOnly = true)
    public long countUnread(User user) {
        return messageRepository.countUnreadForUser(user.getId());
    }

    @Transactional
    public ChatRoomResponse getOrCreateChatRoom(User currentUser, CreateChatRoomRequest request) {
        if (currentUser.getId().equals(request.targetUserId())) {
            throw new IllegalArgumentException("Không thể tự tạo cuộc trò chuyện với chính mình");
        }

        ChatRoom chatRoom = chatRoomRepository
                .findBetweenUsers(currentUser.getId(), request.targetUserId())
                .orElseGet(() -> {
                    User target = userRepository.findById(request.targetUserId())
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

                    Room relatedRoom = request.relatedRoomId() != null
                            ? roomRepository.findById(request.relatedRoomId()).orElse(null)
                            : null;

                    ChatRoom newRoom = ChatRoom.builder()
                            .user1(currentUser)
                            .user2(target)
                            .relatedRoom(relatedRoom)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        return toResponse(chatRoom, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyChatRooms(User currentUser) {
        return chatRoomRepository.findAllForUser(currentUser.getId()).stream()
                .map(c -> toResponse(c, currentUser.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(User currentUser, Long chatRoomId, int page, int size) {
        ChatRoom chatRoom = getOwnedChatRoomOrThrow(currentUser.getId(), chatRoomId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messages = messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId(), pageable);
        return messages.getContent().stream().map(this::toMessageResponse).toList();
    }

    @Transactional
    public MessageResponse sendMessage(User sender, SendMessageRequest request) {
        ChatRoom chatRoom = getOwnedChatRoomOrThrow(sender.getId(), request.chatRoomId());

        Message message = Message.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(request.content())
                .messageType(request.messageType() != null ? request.messageType() : MessageType.TEXT)
                .build();

        messageRepository.save(message);
        return toMessageResponse(message);
    }

    @Transactional
    public void markAsRead(User currentUser, Long chatRoomId) {
        ChatRoom chatRoom = getOwnedChatRoomOrThrow(currentUser.getId(), chatRoomId);
        List<Message> unread = messageRepository
                .findByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId(), Pageable.unpaged())
                .getContent().stream()
                .filter(m -> !m.isRead() && !m.getSender().getId().equals(currentUser.getId()))
                .toList();
        unread.forEach(m -> m.setRead(true));
        messageRepository.saveAll(unread);
    }

    private ChatRoom getOwnedChatRoomOrThrow(Long userId, Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện"));
        boolean isParticipant = chatRoom.getUser1().getId().equals(userId)
                || chatRoom.getUser2().getId().equals(userId);
        if (!isParticipant) {
            throw new ForbiddenActionException("Bạn không thuộc cuộc trò chuyện này");
        }
        return chatRoom;
    }

    private ChatRoomResponse toResponse(ChatRoom chatRoom, Long currentUserId) {
        User other = chatRoom.getUser1().getId().equals(currentUserId)
                ? chatRoom.getUser2() : chatRoom.getUser1();

        Message last = messageRepository
                .findByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId(), PageRequest.of(0, 1))
                .getContent().stream().findFirst().orElse(null);

        long unread = messageRepository.countByChatRoomIdAndIsReadFalseAndSenderIdNot(
                chatRoom.getId(), currentUserId);

        return new ChatRoomResponse(
                chatRoom.getId(), other.getId(), DisplayNames.of(other),
                chatRoom.getRelatedRoom() != null ? chatRoom.getRelatedRoom().getId() : null,
                chatRoom.getRelatedRoom() != null ? chatRoom.getRelatedRoom().getTitle() : null,
                last != null ? last.getContent() : null,
                last != null ? last.getCreatedAt() : null,
                unread
        );
    }

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(), message.getChatRoom().getId(), message.getSender().getId(),
                message.getSender().getEmail(), message.getContent(), message.getMessageType(),
                message.isRead(), message.getCreatedAt()
        );
    }
}