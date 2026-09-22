package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.Role;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.repository.UserRepository;
import tqkhanh.project.boardinghomeproject.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.refreshToken();

        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        if (!"refresh".equals(jwtService.extractTokenType(token))) {
            throw new IllegalArgumentException("Token không phải là refresh token");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        if (!jwtService.isTokenValid(token, user)) {
            throw new IllegalArgumentException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user); // rotate luôn refresh token

        return new AuthResponse(newAccessToken, newRefreshToken, user.getEmail(), user.getRole().name());
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        Role role;
        try {
            role = Role.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Role không hợp lệ (chỉ TENANT hoặc LANDLORD)");
        }
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("Không thể tự đăng ký role ADMIN");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không đúng"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole().name());
    }
}