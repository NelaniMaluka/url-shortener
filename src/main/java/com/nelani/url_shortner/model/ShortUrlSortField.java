package com.nelani.url_shortner.model;

import lombok.Getter;

@Getter
public enum ShortUrlSortField {
    CREATED_AT("createdAt"),
    EXPIRES_AT("expiresAt"),
    ACCESS_LIMIT("accessLimit");

    private final String field;

    ShortUrlSortField(String field) {
        this.field = field;
    }

}
