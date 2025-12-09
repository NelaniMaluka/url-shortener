package com.nelani.url_shortner.service.impl;

import com.nelani.url_shortner.model.ShortUrl;
import com.nelani.url_shortner.repository.ShortUrlRepository;
import com.nelani.url_shortner.service.RedirectionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RedirectionServiceImpl implements RedirectionService {

    private final ShortUrlRepository urlRepository;

    public RedirectionServiceImpl(ShortUrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public String redirect(String shortCode) {
        // Get the url
        ShortUrl shortUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Url does not exist."));

        // Increment clicks by 1 and save
        shortUrl.setClicks(shortUrl.getClicks() + 1);
        urlRepository.save(shortUrl);

        return shortUrl.getOriginalUrl();
    }
}
