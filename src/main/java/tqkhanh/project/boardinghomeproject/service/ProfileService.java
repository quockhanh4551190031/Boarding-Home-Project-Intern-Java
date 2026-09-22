package tqkhanh.project.boardinghomeproject.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tqkhanh.project.boardinghomeproject.dto.ProfileResponse;
import tqkhanh.project.boardinghomeproject.dto.ProfileUpdateRequest;
import tqkhanh.project.boardinghomeproject.entity.Profile;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.repository.ProfileRepository;
import tqkhanh.project.boardinghomeproject.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileResponse getMyProfile(User currentUser){
        Profile profile = profileRepository.findByUserId(currentUser.getId()).orElse(null);
        return toResponse(currentUser, profile);
    }

    @Transactional
    public ProfileResponse updateMyProfile(User currentUser, ProfileUpdateRequest request){
        if(request.phone()!=null && !request.phone().isBlank()){
            profileRepository.findByUserId(currentUser.getId())
                    .ifPresentOrElse(
                            p -> checkPhoneNotTakenByOther(request.phone(), p.getId()),
                            () -> checkPhoneNotTakenByOther(request.phone(),null)
                    );
        }

        Profile profile = profileRepository.findByUserId(currentUser.getId())
                .orElse(Profile.builder().user(currentUser).build());

        profile.setFullName(request.fullName());
        profile.setPhone(request.phone());
        profile.setGender(request.gender());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setAddress(request.address());
        profile.setIdentityNumber(request.identityNumber());
        profile.setBio(request.bio());

        profileRepository.save(profile);

        if(!currentUser.isProfileCompleted()) {
            currentUser.setProfileCompleted(true);
            userRepository.save(currentUser);
        }

        return toResponse(currentUser, profile);
    }

    private void checkPhoneNotTakenByOther(String phone, Long currentProfileId) {
        profileRepository.findAll().stream()
                .filter(p -> phone.equals(p.getPhone()))
                .filter(p -> currentProfileId == null || !p.getId().equals(currentProfileId))
                .findAny()
                .ifPresent(p -> { throw new IllegalArgumentException("Số điện thoại đã được sử dụng"); });
    }

    private ProfileResponse toResponse(User user, Profile profile){
        if (profile == null){
            return new ProfileResponse(user.getEmail(), user.getRole().name(),
                    null, null, null, null, null, null, null, null, false);
        }

        return new ProfileResponse(
                user.getEmail(), user.getRole().name(),
                profile.getFullName(), profile.getPhone(), profile.getAvatarUrl(),
                profile.getGender(), profile.getDateOfBirth(), profile.getAddress(),
                profile.getIdentityNumber(), profile.getBio(), user.isProfileCompleted()
        );
    }
}
