package com.nelani.url_shortner.controller;

import com.nelani.url_shortner.dto.UrlAccessStats;
import com.nelani.url_shortner.model.SortDirection;
import com.nelani.url_shortner.model.StatsGroupBy;
import com.nelani.url_shortner.service.RequestDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/request-data")
public class RequestDataController {

    private final RequestDataService requestDataService;

    public RequestDataController(RequestDataService requestDataService) {
        this.requestDataService = requestDataService;
    }

    @Operation(summary = "Get access statistics", description = "Returns aggregated access stats grouped by URL, country, city, referrer, or user agent.")
    @ApiResponse(responseCode = "200", description = "Paginated access statistics")
    @GetMapping("/stats")
    public ResponseEntity<Page<UrlAccessStats>> stats(
            @Parameter(description = "Group statistics by this dimension", example = "COUNTRY") @RequestParam(defaultValue = "COUNTRY") StatsGroupBy groupBy,

            @Parameter(description = "Page number, zero-based", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Number of records per page", example = "10") @RequestParam(defaultValue = "10") @Max(100) int size,

            @Parameter(description = "Sort direction: ASC or DESC", example = "DESC") @RequestParam(defaultValue = "DESC") SortDirection direction) {
        return ResponseEntity.ok(requestDataService.getTopStats(groupBy, page, size, direction));
    }
}
