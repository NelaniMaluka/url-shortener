package com.nelani.url_shortner.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents request data for a shortened URL access.")
public record RequestDataResponse(
                @Schema(description = "The shortened URL that was accessed", example = "http://localhost:8080/a8f3Ks") String shortUrl,
                @Schema(description = "Hashed identifier of the requester’s device (SHA-256 of IP, user agent, and short URL ID). Falls back to a random UUID if hashing fails.", example = "3a5f1b2c4d6e7f890123456789abcdef0123456789abcdef0123456789abcdef") String hashedDevice,
                @Schema(description = "Country of the requester", example = "United States") String country,
                @Schema(description = "City of the requester", example = "New York") String city,
                @Schema(description = "Referrer URL", example = "https://www.google.com") String referrer,
                @Schema(description = "User agent string", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)") String userAgent,
                @Schema(description = "Timestamp when the request was made", example = "2025-12-06T14:23:00") LocalDateTime timestamp) {
}
