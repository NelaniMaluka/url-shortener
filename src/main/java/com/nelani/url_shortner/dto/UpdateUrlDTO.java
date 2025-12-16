package com.nelani.url_shortner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "DTO for updating an existing shortened URL using the full short URL")
public record UpdateUrlDTO(

                @NotBlank(message = "Short URL cannot be blank") @Schema(description = "Full shortened URL to update", example = "http://localhost:8080/uA0kmXQM") String shortUrl,

                @NotBlank(message = "New URL cannot be blank") @Schema(description = "New full URL to replace the existing one", example = "https://www.google.com") String newUrl,

                @Schema(description = """
                                Number of days until the short URL expires.
                                Allowed values: 1, 7, 15, 30.
                                Leave null for never expiring.
                                """, example = "30", nullable = true) @Min(1) @Max(30) Integer expiresInDays,

                @Schema(description = """
                                Custom short key provided by the user.
                                If not provided, the system will generate one automatically.
                                Length: 2–12 characters.
                                Allowed characters: letters, numbers, '-' and '_'.
                                Must be unique.
                                """, example = "myKey_12", nullable = true) @Pattern(regexp = "^[a-zA-Z0-9_-]{2,12}$", message = "Short key must be between 2 and 12 characters and contain only letters, numbers, '-' or '_'") String newShortKey,

                @Schema(description = """
                                Maximum number of unique devices allowed to access the short URL.
                                Leave null for unlimited access.
                                """, example = "5", nullable = true) @Positive Long accessLimit

) {
}
