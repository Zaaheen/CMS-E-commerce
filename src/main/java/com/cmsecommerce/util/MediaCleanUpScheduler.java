package com.cmsecommerce.util;

import com.cmsecommerce.service.IMediaAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MediaCleanUpScheduler {

    private final IMediaAdminService mediaAdminService;

    // Chạy ngầm tự động vào lúc 02:00 AM hàng ngày
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduleMediaCleanup() {
        log.info("===> Bắt đầu tiến trình tự động dọn dẹp tệp media rác trên Cloudinary...");
        int count = mediaAdminService.cleanupOrphanMediaFiles();
        log.info("===> Hoàn tất dọn dẹp media rác. Số lượng tệp đã xóa trên Cloudinary: {}", count);
    }
}
