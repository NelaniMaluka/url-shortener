package com.nelani.url_shortner.service.impl;

import com.nelani.url_shortner.dto.UrlAccessStats;
import com.nelani.url_shortner.dto.UrlAccessStatsDTO;
import com.nelani.url_shortner.model.SortDirection;
import com.nelani.url_shortner.model.StatsGroupBy;
import com.nelani.url_shortner.repository.RequestDataRepository;
import com.nelani.url_shortner.service.RequestDataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestDataServiceImpl implements RequestDataService {

    private final RequestDataRepository requestDataRepository;

    public RequestDataServiceImpl(RequestDataRepository requestDataRepository) {
        this.requestDataRepository = requestDataRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UrlAccessStats> getTopStats(
            StatsGroupBy groupBy,
            int page, int size, SortDirection direction) {

        // Build dynamic sorting based on access count alias exposed by projections
        Sort sort = Sort.by(
                direction == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC,
                "accessCount");

        // Create pageable request with sorting applied
        Pageable pageable = PageRequest.of(page, size, sort);

        // Delegate aggregation logic to repository layer based on grouping dimension
        Page<UrlAccessStats> data = switch (groupBy) {
            case URL -> requestDataRepository.mostAccessedUrls(pageable);
            case COUNTRY -> requestDataRepository.mostAccessedCountries(pageable);
            case CITY -> requestDataRepository.mostAccessedCities(pageable);
            case REFERRER -> requestDataRepository.mostAccessedReferrers(pageable);
            case USER_AGENT -> requestDataRepository.mostAccessedUserAgents(pageable);
            default -> requestDataRepository.accessStatsByDay(pageable);
        };

        // Only manipulate value to generate the url
        if (groupBy == StatsGroupBy.URL) {
            return data.map(stats -> {
                String modifiedValue = UrlShortenerAlgorithm.buildUrl(stats.getValue());
                return new UrlAccessStatsDTO(modifiedValue, stats.getAccessCount(), stats.getDeviceCount());
            });
        }

        return data;
    }

}
