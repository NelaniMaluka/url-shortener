package com.nelani.url_shortner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for updating an existing shortened URL using the full short URL")
public record UpdateUrlDTO(

        @NotBlank(message = "Short URL cannot be blank") @Schema(description = "Full shortened URL to update", example = "http://localhost:8080/uA0kmXQM") String shortUrl,

        @NotBlank(message = "New URL cannot be blank") @Schema(description = "New full URL to replace the existing one", example = "https://www.google.com") String newUrl

) {
}
