package com.nelani.url_shortner.service;

import com.nelani.url_shortner.dto.UrlAccessStats;
import com.nelani.url_shortner.model.SortDirection;
import com.nelani.url_shortner.model.StatsGroupBy;
import org.springframework.data.domain.Page;

public interface RequestDataService {

    Page<UrlAccessStats> getTopStats(
            StatsGroupBy groupBy,
            int page, int size, SortDirection direction);
}
