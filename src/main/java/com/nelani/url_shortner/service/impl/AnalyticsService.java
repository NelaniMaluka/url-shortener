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
import org.springframework.transaction.annotation.Transactional;

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
    @Async
    @Transactional
    public void logRequestAsync(ShortUrl shortUrl, HttpServletRequest req) {
        String ip = req.getRemoteAddr();

        RequestData data = RequestData.builder()
                .shortUrl(shortUrl)
                .ipAddress(ip)
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
}
