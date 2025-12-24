package com.nelani.url_shortner.service.impl;

import com.nelani.url_shortner.dto.GeoInfo;
import com.nelani.url_shortner.model.RequestData;
import com.nelani.url_shortner.model.ShortUrl;
import com.nelani.url_shortner.repository.RequestDataRepository;
import com.nelani.url_shortner.service.GeoLookupService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Log4j2
@Service
public class AnalyticsService {

    private final GeoLookupService geoLookupService;
    private final RequestDataRepository requestDataRepository;

    public AnalyticsService(GeoLookupService geoLookupService,
            RequestDataRepository requestDataRepository) {
        this.geoLookupService = geoLookupService;
        this.requestDataRepository = requestDataRepository;
    }

    /**
     * Asynchronously logs request metadata so redirect performance
     * is not affected. Analytics is best-effort and should never
     * block the user request flow.
     */
    @Async("analyticsExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRequestAsync(ShortUrl shortUrl, HttpServletRequest req) {
        final String ip = getClientIp(req);
        final String deviceHash = generateDeviceHash(ip, req.getHeader("User-Agent"), shortUrl.getId());

        RequestData data = RequestData.builder()
                .shortUrl(shortUrl)
                .deviceHash(deviceHash)
                .referrer(req.getHeader("Referer"))
                .userAgent(req.getHeader("User-Agent"))
                .build();

        // Geo lookup, failure should not affect the redirect.
        try {
            GeoInfo geo = geoLookupService.lookup(ip);
            if (geo != null) {
                data.setCountry(geo.country());
                data.setCity(geo.city());
            }
        } catch (Exception ex) {
            log.warn("Geo lookup failed for IP={} : {}", ip, ex.getMessage());
        }

        // Save analytics data, failure is logged but not rethrown.
        try {
            requestDataRepository.save(data);
        } catch (Exception ex) {
            log.error("Failed to persist request analytics for shortUrl={} : {}",
                    shortUrl.getId(), ex.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String generateDeviceHash(String ip, String userAgent, UUID shortUrlId) {
        try {
            final String raw = ip + "|" + userAgent + "|" + shortUrlId;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            // Fallback: log the error and generate a random UUID
            log.error("Warning: Could not generate device hash, using fallback UUID. " + e.getMessage());
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

}
