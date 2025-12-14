package com.nelani.url_shortner.mapper;

import com.nelani.url_shortner.model.ShortUrl;
import com.nelani.url_shortner.repository.RequestDataRepository;
import com.nelani.url_shortner.response.UrlResponse;
import com.nelani.url_shortner.service.impl.UrlShortenerAlgorithm;

public class UrlResponseMapper {

    public static UrlResponse toDto(ShortUrl shortUrl, int clicks) {
        return new UrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                UrlShortenerAlgorithm.buildUrl(shortUrl.getShortCode()),
                shortUrl.getCreatedAt(),
                shortUrl.getUpdatedAt(),
                shortUrl.getExpiresAt(),
                clicks);
    }

    // Overloaded method for mapping without clicks
    public static UrlResponse toDto(ShortUrl shortUrl, RequestDataRepository requestDataRepository) {
        int clicks = requestDataRepository.countByShortUrl(shortUrl);
        return toDto(shortUrl, clicks);
    }

}
