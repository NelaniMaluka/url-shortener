package com.nelani.url_shortner.mapper;

import com.nelani.url_shortner.response.RequestDataResponse;
import com.nelani.url_shortner.dto.UrlAccessStats;
import com.nelani.url_shortner.model.RequestData;
import com.nelani.url_shortner.response.MostAccessedUrlResponse;
import com.nelani.url_shortner.service.impl.UrlShortenerAlgorithm;

public class RequestDataMapper {

    public static RequestDataResponse toRequestDataDto(RequestData requestData) {
        return new RequestDataResponse(UrlShortenerAlgorithm.buildUrl(requestData.getShortUrl().getShortCode()),
                requestData.getDeviceHash(), requestData.getCountry(), requestData.getCity(), requestData.getReferrer(),
                requestData.getUserAgent(), requestData.getTimestamp());
    }

    public static MostAccessedUrlResponse toMostAccessedUrlDto(UrlAccessStats stats) {
        return new MostAccessedUrlResponse(UrlShortenerAlgorithm.buildUrl(stats.getShortCode()),
                stats.getAccessCount());
    }
}
