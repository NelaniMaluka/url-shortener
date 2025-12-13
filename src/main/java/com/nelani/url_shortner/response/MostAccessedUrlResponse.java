package com.nelani.url_shortner.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "MostAccessedUrlResponse", description = "Represents a short URL and the number of times it has been accessed")
public record MostAccessedUrlResponse(

                @Schema(description = "Fully qualified shortened URL", example = "https://sho.rt/abc123") String shortUrl,

                @Schema(description = "Total number of times the short URL was accessed", example = "154") long accessCount) {
}
