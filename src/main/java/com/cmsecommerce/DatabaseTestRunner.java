package com.cmsecommerce;

import com.cmsecommerce.entity.User;
import com.cmsecommerce.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseTestRunner implements CommandLineRunner {

    private final IUserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("===> Bắt đầu kiểm tra kết nối Supabase PostgreSQL...");

        // Thử thêm 1 User mẫu
        if (userRepository.count() == 0) {
            User testUser = User.builder()
                    .email("admin@codegym.vn")
                    .passwordHash("hashed_password_123")
                    .role("ADMIN")
                    .status("ACTIVE")
                    .build();
            userRepository.save(testUser);
            log.info("===> Đã chèn thành công User test vào Supabase!");
        }

        long totalUsers = userRepository.count();
        log.info("===> Kết nối thành công! Tổng số User hiện có trên Supabase: {}", totalUsers);
    }
}
