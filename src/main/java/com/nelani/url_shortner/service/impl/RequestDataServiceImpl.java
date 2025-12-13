package com.nelani.url_shortner.service.impl;

import com.nelani.url_shortner.mapper.RequestDataMapper;
import com.nelani.url_shortner.repository.RequestDataRepository;
import com.nelani.url_shortner.response.MostAccessedUrlResponse;
import com.nelani.url_shortner.service.RequestDataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RequestDataServiceImpl implements RequestDataService {

    private final RequestDataRepository requestDataRepository;

    public RequestDataServiceImpl(RequestDataRepository requestDataRepository) {
        this.requestDataRepository = requestDataRepository;
    }

    @Override
    public Page<MostAccessedUrlResponse> getMostAccessedRequestData(int page, int size) {
        // Get the most accessed urls
        Pageable pageable = PageRequest.of(page, size);
        var requestDataPage = requestDataRepository.findMostAccessedUrls(pageable);

        // Return the page with urls mapped to the dto
        return requestDataPage.map(RequestDataMapper::toMostAccessedUrlDto);
    }
}
