package com.nelani.url_shortner.service;

import com.nelani.url_shortner.response.MostAccessedUrlResponse;
import org.springframework.data.domain.Page;

public interface RequestDataService {
    Page<MostAccessedUrlResponse> getMostAccessedRequestData(int page, int size);
}
