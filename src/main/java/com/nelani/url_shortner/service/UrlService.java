package com.nelani.url_shortner.service;

import com.nelani.url_shortner.dto.UpdateUrlDTO;
import com.nelani.url_shortner.response.UrlResponse;
import org.springframework.data.domain.Page;

public interface UrlService {

    Page<UrlResponse> viewAllUrls(int page, int size);

    UrlResponse createShortUrl(String url);

    UrlResponse updateUrl(UpdateUrlDTO dto);

    void deleteUrl(String url);
}
