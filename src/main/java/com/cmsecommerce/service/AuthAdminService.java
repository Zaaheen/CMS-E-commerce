package com.cmsecommerce.service;

import com.cmsecommerce.dto.request.LoginRequestDTO;
import com.cmsecommerce.dto.response.AuthResponse;
import com.cmsecommerce.dto.response.UserResponseDTO;
import com.cmsecommerce.entity.User;
import com.cmsecommerce.exception.ResourceNotFoundException;
import com.cmsecommerce.repository.IUserRepository;
import com.cmsecommerce.security.user.CustomUserDetails;
import com.cmsecommerce.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthAdminService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final IUserRepository userRepository;

    public AuthResponse login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(userDetails.getId())
                .email(userDetails.getEmail())
                .role(userDetails.getRole())
                .build();
    }

    public UserResponseDTO getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
