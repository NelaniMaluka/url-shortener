package com.nelani.url_shortner.service;

import com.nelani.url_shortner.dto.GeoInfo;

public interface GeoLookupService {
    GeoInfo lookup(String ipAddress);
}
