package com.nelani.url_shortner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "DTO for creating a shortened URL")
public record CreateUrlDTO(

                @NotBlank(message = "URL cannot be blank") @Schema(description = "Full URL to shorten", example = "https://www.google.com") String url,

                @Schema(description = """
                                Number of days until the short URL expires.
                                Allowed values: 1, 7, 15, 30.
                                Leave empty or null for never expiring.
                                """, example = "30", nullable = true) @Min(1) @Max(30) Integer expiresInDays,

                @Schema(description = """
                                Maximum number of unique devices allowed to access the short URL.
                                Leave null for unlimited access.
                                """, example = "5", nullable = true) @Positive Long accessLimit) {

}
