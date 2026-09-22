package tqkhanh.project.boardinghomeproject.service;


import tqkhanh.project.boardinghomeproject.dto.FavoriteResponse;
import tqkhanh.project.boardinghomeproject.dto.RoomResponse;
import tqkhanh.project.boardinghomeproject.entity.Favorite;
import tqkhanh.project.boardinghomeproject.entity.Room;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.exception.ResourceNotFoundException;
import tqkhanh.project.boardinghomeproject.repository.FavoriteRepository;
import tqkhanh.project.boardinghomeproject.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;

    @Transactional
    public FavoriteResponse addFavorite(User user, Long roomId) {
        if (favoriteRepository.existsByUserIdAndRoomId(user.getId(), roomId)) {
            throw new IllegalArgumentException("Phòng này đã có trong danh sách yêu thích");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng trọ"));

        Favorite favorite = Favorite.builder()
                .user(user)
                .room(room)
                .build();

        favoriteRepository.save(favorite);

        RoomResponse roomResponse = roomService.getByIdWithoutIncrementingView(roomId);
        return new FavoriteResponse(favorite.getId(),roomResponse,favorite.getCreatedAt());
    }

    @Transactional
    public void removeFavorite(User user, Long roomId) {
        Favorite favorite = favoriteRepository.findByUserIdAndRoomId(user.getId(), roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng này chưa có trong danh sách yêu thích"));
        favoriteRepository.delete(favorite);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getMyFavorites(User user) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(fav -> {
                    RoomResponse roomResponse = roomService.getByIdWithoutIncrementingView(fav.getRoom().getId());
                    return new FavoriteResponse(fav.getId(), roomResponse, fav.getCreatedAt());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String,Boolean> isFavorited(User user, Long roomId) {
        return Map.of("favorited",favoriteRepository.existsByUserIdAndRoomId(user.getId(),roomId));
    }
}
