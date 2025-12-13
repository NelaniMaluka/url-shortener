package com.nelani.url_shortner.controller;

import com.nelani.url_shortner.response.MostAccessedUrlResponse;
import com.nelani.url_shortner.service.RequestDataService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary = "Get most accessed request data", description = "Returns a paginated list of request data entries for the most accessed shortened URL.")
    @ApiResponse(responseCode = "200", description = "Most accessed URL", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MostAccessedUrlResponse.class)))
    @GetMapping("/most-accessed")
    @RateLimiter(name = "requestDataRateLimiter")
    public ResponseEntity<Page<?>> getMostAccessedRequestData(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size) {
        Page<MostAccessedUrlResponse> response = requestDataService.getMostAccessedRequestData(page, size);
        return ResponseEntity.ok(response);
    }
}
