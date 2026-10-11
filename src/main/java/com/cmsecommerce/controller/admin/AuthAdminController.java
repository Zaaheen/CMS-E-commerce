package com.cmsecommerce.controller.admin;

import com.cmsecommerce.dto.request.LoginRequestDTO;
import com.cmsecommerce.dto.response.ApiResponse;
import com.cmsecommerce.dto.response.AuthResponse;
import com.cmsecommerce.dto.response.UserResponseDTO;
import com.cmsecommerce.security.user.CustomUserDetails;
import com.cmsecommerce.service.AuthAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AuthAdminController {

    private final AuthAdminService authAdminService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid  @RequestBody LoginRequestDTO request) {
        AuthResponse response = authAdminService.login(request);
        return ResponseEntity.ok(ApiResponse.success( "Đăng nhập thành công", response));
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponseDTO userResponse = authAdminService.getCurrentUser(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }
}
