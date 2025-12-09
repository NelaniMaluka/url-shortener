package com.nelani.url_shortner.mapper;

import com.nelani.url_shortner.model.ShortUrl;
import com.nelani.url_shortner.response.UrlResponse;
import com.nelani.url_shortner.service.impl.UrlShortenerAlgorithm;

public class UrlResponseMapper {

    public static UrlResponse toDto(ShortUrl shortUrl) {
        return new UrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                UrlShortenerAlgorithm.buildUrl(shortUrl.getShortCode()),
                shortUrl.getCreatedAt(),
                shortUrl.getUpdatedAt(),
                shortUrl.getClicks());
    }

}
