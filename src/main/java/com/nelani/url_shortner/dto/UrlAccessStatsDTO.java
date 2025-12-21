package com.nelani.url_shortner.dto;

public class UrlAccessStatsDTO implements UrlAccessStats {

    private String value;
    private final long accessCount;
    private final long deviceCount;

    public UrlAccessStatsDTO(String value, long accessCount, long deviceCount) {
        this.value = value;
        this.accessCount = accessCount;
        this.deviceCount = deviceCount;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public long getAccessCount() {
        return accessCount;
    }

    @Override
    public long getDeviceCount() {
        return deviceCount;
    }
}
