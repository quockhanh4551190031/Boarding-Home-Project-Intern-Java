package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.dto.ProfileResponse;
import tqkhanh.project.boardinghomeproject.dto.ProfileUpdateRequest;
import tqkhanh.project.boardinghomeproject.entity.Profile;
import tqkhanh.project.boardinghomeproject.entity.Role;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.exception.ResourceNotFoundException;
import tqkhanh.project.boardinghomeproject.repository.ProfileRepository;
import tqkhanh.project.boardinghomeproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(User currentUser) {
        User user = loadUser(currentUser.getId());
        return toResponse(user, profileRepository.findByUserId(user.getId()).orElse(null));
    }

    @Transactional
    public ProfileResponse updateMyProfile(User currentUser, ProfileUpdateRequest request) {
        User user = loadUser(currentUser.getId());
        String phone = clean(request.phone());

        // Người thuê cần số này để liên hệ chủ trọ nên không cho để trống
        if (user.getRole() == Role.LANDLORD && phone == null) {
            throw new IllegalArgumentException(
                    "Chủ trọ bắt buộc phải có số điện thoại để người thuê liên hệ. Không thể lưu hồ sơ khi để trống.");
        }
        if (phone != null && profileRepository.existsByPhoneAndUserIdNot(phone, user.getId())) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
        }

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> Profile.builder().user(user).build());

        profile.setFullName(request.fullName().trim());
        profile.setPhone(phone);
        profile.setGender(request.gender());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setAddress(clean(request.address()));
        profile.setIdentityNumber(clean(request.identityNumber()));
        profile.setBio(clean(request.bio()));

        try {
            profile = profileRepository.saveAndFlush(profile);
        } catch (DataIntegrityViolationException e) {
            // Hai yêu cầu lưu cùng số cùng lúc: ràng buộc unique ở DB chặn bản thứ hai
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
        }

        if (!user.isProfileCompleted()) {
            user.setProfileCompleted(true);
        }
        return toResponse(user, profile);
    }

    private User loadUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ProfileResponse toResponse(User user, Profile profile) {
        if (profile == null) {
            return new ProfileResponse(user.getId(), user.getEmail(), user.getRole().name(),
                    null, null, null, null, null, null, null, null, false);
        }
        return new ProfileResponse(
                user.getId(), user.getEmail(), user.getRole().name(),
                profile.getFullName(), profile.getPhone(), profile.getAvatarUrl(),
                profile.getGender(), profile.getDateOfBirth(), profile.getAddress(),
                profile.getIdentityNumber(), profile.getBio(), user.isProfileCompleted()
        );
    }
}