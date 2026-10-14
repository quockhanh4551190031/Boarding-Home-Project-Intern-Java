package tqkhanh.project.boardinghomeproject.security;

import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String CHAT_TOPIC_PREFIX = "/topic/chat/";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (!canSubscribe(accessor)) {
                return null; // bỏ qua yêu cầu nghe lén phòng chat của người khác
            }
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return;

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (jwtService.isTokenValid(token, userDetails)) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            accessor.setUser(authentication);
        }
    }

    /** Kênh chat chỉ dành cho 2 người trong phòng đó. Các kênh khác không bị ảnh hưởng. */
    private boolean canSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(CHAT_TOPIC_PREFIX)) return true;

        Principal principal = accessor.getUser();
        if (!(principal instanceof Authentication auth) || !(auth.getPrincipal() instanceof User user)) {
            return false;
        }

        try {
            Long roomId = Long.parseLong(destination.substring(CHAT_TOPIC_PREFIX.length()));
            return chatRoomRepository.isParticipant(roomId, user.getId());
        } catch (NumberFormatException e) {
            return false;
        }
    }
}