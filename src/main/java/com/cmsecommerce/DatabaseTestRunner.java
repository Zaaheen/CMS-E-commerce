package com.cmsecommerce;

import com.cmsecommerce.entity.User;
import com.cmsecommerce.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseTestRunner implements CommandLineRunner {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("===> Bắt đầu kiểm tra kết nối Supabase PostgreSQL...");

        // Tìm hoặc tạo user admin, luôn đảm bảo password được hash BCrypt
        User adminUser = userRepository.findByEmail("admin@codegym.vn").orElse(null);
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        if (adminUser == null) {
            adminUser = User.builder()
                    .email("admin@codegym.vn")
                    .passwordHash(encodedPassword)
                    .role("ADMIN")
                    .status("ACTIVE")
                    .build();
            userRepository.save(adminUser);
            log.info("===> Đã tạo mới User admin!");
        } else if (!passwordEncoder.matches(rawPassword, adminUser.getPasswordHash())) {
            // Password chưa phải BCrypt hoặc khác mật khẩu mặc định -> update
            adminUser.setPasswordHash(encodedPassword);
            userRepository.save(adminUser);
            log.info("===> Đã cập nhật password cho user admin thành BCrypt hash!");
        } else {
            log.info("===> User admin đã có password BCrypt hợp lệ");
        }

        log.info("===> Tài khoản test: admin@codegym.vn / 123456");

        long totalUsers = userRepository.count();
        log.info("===> Kết nối thành công! Tổng số User hiện có trên Supabase: {}", totalUsers);
    }
}
