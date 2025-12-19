package com.nelani.url_shortner.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public interface UrlAccessStats {

        @Schema(description = "Grouped value (short URL, country, city, referrer, user agent, or date)", example = "abc123")
        String getValue();

        @Schema(description = "Total number of times this value was accessed", example = "154")
        long getAccessCount();

        @Schema(description = "Total number of distinct devices that accessed this value", example = "97")
        long getDeviceCount();
}
