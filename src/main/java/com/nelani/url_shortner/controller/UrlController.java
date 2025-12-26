package com.nelani.url_shortner.controller;

import com.nelani.url_shortner.dto.CreateUrlDTO;
import com.nelani.url_shortner.dto.UpdateUrlDTO;
import com.nelani.url_shortner.model.ShortUrlSortField;
import com.nelani.url_shortner.model.SortDirection;
import com.nelani.url_shortner.response.BulkUrlResult;
import com.nelani.url_shortner.response.UrlResponse;
import com.nelani.url_shortner.service.UrlService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class UrlController {

        private final UrlService urlService;

        public UrlController(UrlService urlService) {
                this.urlService = urlService;
        }

        @Operation(summary = "View all shortened URLs", description = "Returns a paginated list of all URLs.")
        @ApiResponse(responseCode = "200", description = "List retrieved successfully")
        @GetMapping("/urls")
        @RateLimiter(name = "shortenRateLimiter")
        public ResponseEntity<?> viewAllUrls(
                        @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field: CREATED_AT, EXPIRES_AT, ACCESS_LIMIT") @RequestParam(defaultValue = "CREATED_AT") ShortUrlSortField sortBy,

                        @Parameter(description = "Sort direction: ASC or DESC") @RequestParam(defaultValue = "DESC") SortDirection direction) {
                var urls = urlService.viewAllUrls(page, size, sortBy, direction);
                return ResponseEntity.ok(urls);
        }

        @Operation(summary = "Create shortened URLs in bulk")
        @ApiResponse(responseCode = "200", description = "Short URLs created", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BulkUrlResult.class))))
        @PostMapping("/urls/add")
        @RateLimiter(name = "shortenRateLimiter")
        public ResponseEntity<List<BulkUrlResult>> addUrlsBulk(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of URLs to shorten", required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = CreateUrlDTO.class)))) @RequestBody List<CreateUrlDTO> urls) {

                List<BulkUrlResult> results = urls.stream()
                                .map(createUrlDTO -> {
                                        try {
                                                return new BulkUrlResult(
                                                                createUrlDTO,
                                                                urlService.createShortUrl(createUrlDTO),
                                                                null);
                                        } catch (Exception e) {
                                                return new BulkUrlResult(
                                                                createUrlDTO,
                                                                null,
                                                                e.getMessage());
                                        }
                                })
                                .toList();

                return ResponseEntity.ok(results);
        }

        @Operation(summary = "Update an existing shortened URL", description = "Replaces an existing URL mapping with a new URL.")
        @ApiResponse(responseCode = "200", description = "URL updated", content = @Content(schema = @Schema(implementation = UrlResponse.class)))
        @PutMapping("/urls/update")
        @RateLimiter(name = "shortenRateLimiter")
        public ResponseEntity<?> updateUrl(
                        @Parameter(description = "New replacement URL") @RequestBody UpdateUrlDTO dto) {
                UrlResponse response = urlService.updateUrl(dto);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Delete a shortened URL", description = "Deletes a URL mapping from the system.")
        @ApiResponse(responseCode = "204", description = "URL deleted")
        @DeleteMapping("/urls/delete")
        @RateLimiter(name = "shortenRateLimiter")
        public ResponseEntity<Void> removeUrl(
                        @Parameter(description = "URL to delete") @RequestBody String url) {
                urlService.deleteUrl(url);
                return ResponseEntity.noContent().build();
        }

}
