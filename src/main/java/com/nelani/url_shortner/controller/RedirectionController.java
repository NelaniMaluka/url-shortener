package com.nelani.url_shortner.controller;

import com.nelani.url_shortner.service.RedirectionService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class RedirectionController {

        private final RedirectionService redirectionService;

        public RedirectionController(RedirectionService redirectionService) {
                this.redirectionService = redirectionService;
        }

        @Operation(summary = "Redirect short URL to original URL", description = "Takes the short code and performs an HTTP 302 redirect to the corresponding long URL.")
        @ApiResponse(responseCode = "302", description = "Redirect to the original URL")
        @GetMapping("{shortCode}")
        @RateLimiter(name = "redirectRateLimiter")
        public ResponseEntity<Void> redirect(
                        @Parameter(description = "The short code generated for the long URL", example = "a8f3Ks") @PathVariable String shortCode,

                        HttpServletRequest req) {
                String longUrl = redirectionService.redirect(shortCode, req);

                return ResponseEntity.status(302)
                                .header("Location", longUrl)
                                .build();
        }
}
