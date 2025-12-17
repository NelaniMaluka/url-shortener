package com.nelani.url_shortner.service.impl;

import com.nelani.url_shortner.dto.CreateUrlDTO;
import com.nelani.url_shortner.dto.UpdateUrlDTO;
import com.nelani.url_shortner.mapper.UrlResponseMapper;
import com.nelani.url_shortner.model.ShortUrl;
import com.nelani.url_shortner.model.ShortUrlSortField;
import com.nelani.url_shortner.model.SortDirection;
import com.nelani.url_shortner.repository.RequestDataRepository;
import com.nelani.url_shortner.repository.ShortUrlRepository;
import com.nelani.url_shortner.response.UrlResponse;
import com.nelani.url_shortner.service.UrlService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class UrlServiceImpl implements UrlService {

    private final ShortUrlRepository urlRepository;
    private final RequestDataRepository requestDataRepository;

    public UrlServiceImpl(ShortUrlRepository urlRepository, RequestDataRepository requestDataRepository) {
        this.urlRepository = urlRepository;
        this.requestDataRepository = requestDataRepository;
    }

    @Override
    @Transactional
    public Page<UrlResponse> viewAllUrls(int page, int size, ShortUrlSortField sortField,
            SortDirection direction) {

        Sort sort = Sort.by(
                direction == SortDirection.ASC
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                sortField.getField());

        // Generate and get the urls
        Pageable pageable = PageRequest.of(page, size, sort);
        var urls = urlRepository.findAll(pageable);

        // Return the page with urls mapped to the dto
        return urls.map(shortUrl -> UrlResponseMapper.toDto(shortUrl, requestDataRepository));
    }

    @Override
    @Transactional
    public UrlResponse createShortUrl(CreateUrlDTO dto) {
        // validate url
        String url = UrlShortenerAlgorithm.validateUrl(dto.url());

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
                .accessLimit(dto.accessLimit())
                .build();

        // Set the expiration date
        shortUrl.setExpiresAt(resolveExpiry(dto.expiresInDays()));

        urlRepository.save(shortUrl);

        Long clicks = requestDataRepository.countByShortUrl(shortUrl);

        // Return the url mapped to the dto
        return UrlResponseMapper.toDto(shortUrl, clicks);
    }

    @Override
    @Transactional
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

        // Check if the new short code exists
        if (dto.newShortKey() != null && !urlRepository.existsByShortCode(dto.newShortKey())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Short Key is already in use.");
        }

        // Set the new short code if it's not null
        if (dto.newShortKey() != null) {
            shortUrl.setShortCode(dto.newShortKey());
        }

        // Set the expiration date
        shortUrl.setExpiresAt(resolveExpiry(dto.expiresInDays()));
        shortUrl.setAccessLimit(dto.accessLimit());

        // update the url entity with the new url while maintaining the old shortCode
        shortUrl.setOriginalUrl(newUrl);
        urlRepository.save(shortUrl);

        Long clicks = requestDataRepository.countByShortUrl(shortUrl);

        // Return the url mapped to the dto
        return UrlResponseMapper.toDto(shortUrl, clicks);
    }

    @Override
    @Transactional
    public void deleteUrl(String existingUrl) {
        // Validate url
        existingUrl = UrlShortenerAlgorithm.validateUrl(existingUrl);

        // Gets the shortCode from the url
        String shortCode = UrlShortenerAlgorithm.decode(existingUrl);

        // Checks if the urls exists
        ShortUrl shortUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short url does not exist."));

        // Deletes the url from the database
        requestDataRepository.deleteByShortUrl(shortUrl);
        urlRepository.delete(shortUrl);
    }

    private LocalDateTime resolveExpiry(Integer days) {
        if (days == null) {
            return null; // never expires
        }

        return switch (days) {
            case 1 -> LocalDateTime.now().plusDays(1);
            case 7 -> LocalDateTime.now().plusDays(7);
            case 15 -> LocalDateTime.now().plusDays(15);
            case 30 -> LocalDateTime.now().plusDays(30);
            default -> throw new IllegalArgumentException(
                    "Invalid expiresInDays value. Allowed values: 1, 7, 15, 30");
        };
    }
}
