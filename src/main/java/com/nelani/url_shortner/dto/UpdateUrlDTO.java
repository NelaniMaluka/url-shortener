package com.nelani.url_shortner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for updating an existing shortened URL using the full short URL")
public record UpdateUrlDTO(

                @NotBlank(message = "Short URL cannot be blank") @Schema(description = "Full shortened URL to update", example = "http://localhost:8080/uA0kmXQM") String shortUrl,

                @NotBlank(message = "New URL cannot be blank") @Schema(description = "New full URL to replace the existing one", example = "https://www.google.com") String newUrl,

                @Schema(description = """
                                Number of days until the short URL expires.
                                Allowed values: 1, 7, 15, 30.
                                Leave empty or null for never expiring.
                                """, example = "30", nullable = true) @Min(1) @Max(30) Integer expiresInDays

) {
}
