package com.nelani.url_shortner.service.impl;

import com.nelani.url_shortner.model.ShortUrl;
import com.nelani.url_shortner.repository.RequestDataRepository;
import com.nelani.url_shortner.repository.ShortUrlRepository;
import com.nelani.url_shortner.service.RedirectionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class RedirectionServiceImpl implements RedirectionService {

    private final ShortUrlRepository urlRepository;
    private final AnalyticsService analyticsService;
    private final RequestDataRepository requestDataRepository;

    public RedirectionServiceImpl(ShortUrlRepository urlRepository, AnalyticsService analyticsService,
            RequestDataRepository requestDataRepository) {
        this.urlRepository = urlRepository;
        this.analyticsService = analyticsService;
        this.requestDataRepository = requestDataRepository;
    }

    @Override
    @Transactional
    public String redirect(String shortCode, HttpServletRequest req) {
        // Get the url
        ShortUrl shortUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Url does not exist."));

        // Check if the url is expired
        if (shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Short URL has expired.");
        }

        // Check if the limit is not exceeded
        if (shortUrl.getAccessLimit() != null) {
            long accessedDevices = requestDataRepository.countDistinctDeviceHashes(shortUrl.getId());

            if (accessedDevices >= shortUrl.getAccessLimit()) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Access denied: this short URL has reached its device limit of " + shortUrl.getAccessLimit());
            }
        }

        // Log analytics asynchronously, any failure here does NOT block redirect
        analyticsService.logRequestAsync(shortUrl, req);

        // Return the original URL for redirection
        return shortUrl.getOriginalUrl();
    }
}
