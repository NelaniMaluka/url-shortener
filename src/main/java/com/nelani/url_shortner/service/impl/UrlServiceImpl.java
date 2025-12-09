package com.nelani.url_shortner.service.impl;

import com.nelani.url_shortner.dto.UpdateUrlDTO;
import com.nelani.url_shortner.mapper.UrlResponseMapper;
import com.nelani.url_shortner.model.ShortUrl;
import com.nelani.url_shortner.repository.ShortUrlRepository;
import com.nelani.url_shortner.response.UrlResponse;
import com.nelani.url_shortner.service.UrlService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UrlServiceImpl implements UrlService {

    private final ShortUrlRepository urlRepository;

    public UrlServiceImpl(ShortUrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Override
    public Page<UrlResponse> viewAllUrls(int page, int size) {
        // Generate and get the urls
        Pageable pageable = PageRequest.of(page, size);
        var urls = urlRepository.findAll(pageable);

        // Return the page with urls mapped to the dto
        return urls.map(UrlResponseMapper::toDto);
    }

    @Override
    public UrlResponse createShortUrl(String url) {
        // validate url
        url = UrlShortenerAlgorithm.validateUrl(url);

        // Check if the url exists
        boolean exists = urlRepository.existsByOriginalUrl(url);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Url already exists.");
        }

        // Generate a shortCode for the new url
        String shortCode;
        do {
            shortCode = UrlShortenerAlgorithm.encode(url);
        } while (urlRepository.existsByShortCode(shortCode));

        // Create and save the new url
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(url)
                .build();
        urlRepository.save(shortUrl);

        // Return the url mapped to the dto
        return UrlResponseMapper.toDto(shortUrl);
    }

    @Override
    public UrlResponse updateUrl(UpdateUrlDTO dto) {
        // validate urls
        String existingUrl = UrlShortenerAlgorithm.validateUrl(dto.shortUrl());
        String newUrl = UrlShortenerAlgorithm.validateUrl(dto.newUrl());

        // Gets the shortCode from the url
        String shortCode = UrlShortenerAlgorithm.decode(existingUrl);

        // Checks if the urls exists
        ShortUrl shortUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short url does not exist."));

        // Checks if the new url exists
        boolean exists = urlRepository.existsByOriginalUrl(newUrl);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Url already exists.");
        }

        // update the url entity with the new url while maintaining the old shortCode
        shortUrl.setOriginalUrl(newUrl);
        urlRepository.save(shortUrl);

        // Return the url mapped to the dto
        return UrlResponseMapper.toDto(shortUrl);
    }

    @Override
    public void deleteUrl(String existingUrl) {
        // validate url
        existingUrl = UrlShortenerAlgorithm.validateUrl(existingUrl);

        // Gets the shortCode from the url
        String shortCode = UrlShortenerAlgorithm.decode(existingUrl);

        // Checks if the urls exists
        ShortUrl shortUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short url does not exist."));

        // deletes the url from the database
        urlRepository.delete(shortUrl);
    }
}
