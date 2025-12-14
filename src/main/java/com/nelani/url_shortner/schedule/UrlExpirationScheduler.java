package com.nelani.url_shortner.schedule;

import com.nelani.url_shortner.repository.ShortUrlRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Log4j2
@Component
public class UrlExpirationScheduler {

    private final ShortUrlRepository shortUrlRepository;

    public UrlExpirationScheduler(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    // Runs every day at 3:00 AM
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void deleteExpiredUrls() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        log.info("Starting expired URL cleanup. Expired before: {}", cutoffDate);

        var expiredUrls = shortUrlRepository.findUrlsExpiredBefore(cutoffDate);
        int count = expiredUrls.size();

        if (count == 0) {
            log.info("No expired URLs found for cleanup.");
            return;
        }

        shortUrlRepository.deleteAll(expiredUrls); // batch delete for efficiency

        log.info("Expired URL cleanup finished. Deleted {} URLs.", count);

        // Optional: log a few sample URLs for traceability without spamming
        expiredUrls.stream()
                .limit(5)
                .forEach(url -> log.debug("Deleted expired URL: {}", url));
    }
}
