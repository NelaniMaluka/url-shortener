package com.nelani.url_shortner.service.impl;

import com.nelani.url_shortner.dto.GeoInfo;
import com.nelani.url_shortner.service.GeoLookupService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Log4j2
@Service
public class GeoLookupServiceImpl implements GeoLookupService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Performs a geo IP lookup using a free external API (ip-api.com).
     * Returns country and city. If the lookup fails, returns "Unknown".
     *
     * This method is safe for production: failures do not throw exceptions
     * to the caller and do not block critical flows.
     *
     * @param ipAddress IP address to lookup
     * @return GeoInfo containing country and city
     */
    @Override
    public GeoInfo lookup(String ipAddress) {
        try {
            String url = "http://ip-api.com/json/" + ipAddress + "?fields=status,country,city";

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            // Skip if IP address is equal to these values
            if (ipAddress == null ||
                    ipAddress.equals("127.0.0.1") ||
                    ipAddress.equals("::1") ||
                    ipAddress.startsWith("0:0:0:0")) {
                log.warn("Geo lookup failed for IP={} : API returned unsuccessful status", ipAddress);
                return new GeoInfo("Unknown", "Unknown");
            }

            String country = (String) response.getOrDefault("country", "Unknown");
            String city = (String) response.getOrDefault("city", "Unknown");

            return new GeoInfo(country, city);

        } catch (Exception ex) {
            log.warn("Geo lookup exception for IP={} : {}", ipAddress, ex.getMessage());
            return new GeoInfo("Unknown", "Unknown");
        }
    }
}
