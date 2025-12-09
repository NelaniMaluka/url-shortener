package com.nelani.url_shortner.controller;

import com.nelani.url_shortner.dto.UpdateUrlDTO;
import com.nelani.url_shortner.response.UrlResponse;
import com.nelani.url_shortner.service.UrlService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

                        @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size) {
                var urls = urlService.viewAllUrls(page, size);
                return ResponseEntity.ok(urls);
        }

        @Operation(summary = "Create a new shortened URL", description = "Takes a full URL and returns a shortened one.")
        @ApiResponse(responseCode = "200", description = "Short URL created", content = @Content(schema = @Schema(implementation = UrlResponse.class)))
        @PostMapping("/urls")
        @RateLimiter(name = "shortenRateLimiter")
        public ResponseEntity<?> addUrl(
                        @Parameter(description = "URL to shorten") @RequestBody String url) {
                UrlResponse response = urlService.createShortUrl(url);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Update an existing shortened URL", description = "Replaces an existing URL mapping with a new URL.")
        @ApiResponse(responseCode = "200", description = "URL updated", content = @Content(schema = @Schema(implementation = UrlResponse.class)))
        @PutMapping("/urls")
        @RateLimiter(name = "shortenRateLimiter")
        public ResponseEntity<?> updateUrl(
                        @Parameter(description = "New replacement URL") @RequestBody UpdateUrlDTO dto) {
                UrlResponse response = urlService.updateUrl(dto);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Delete a shortened URL", description = "Deletes a URL mapping from the system.")
        @ApiResponse(responseCode = "200", description = "URL deleted")
        @DeleteMapping("/urls")
        @RateLimiter(name = "shortenRateLimiter")
        public ResponseEntity<?> removeUrl(
                        @Parameter(description = "URL to delete") @RequestBody String url) {
                urlService.deleteUrl(url);
                return ResponseEntity.ok("Deleted successfully");
        }
}
