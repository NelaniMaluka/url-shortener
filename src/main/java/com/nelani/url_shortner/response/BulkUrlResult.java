package com.nelani.url_shortner.response;

import com.nelani.url_shortner.dto.CreateUrlDTO;

public record BulkUrlResult(
                CreateUrlDTO request,
                UrlResponse response,
                String error) {
}
