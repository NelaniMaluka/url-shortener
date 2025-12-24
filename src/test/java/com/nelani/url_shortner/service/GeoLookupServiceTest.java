package com.nelani.url_shortner.service;

import com.nelani.url_shortner.dto.GeoInfo;
import com.nelani.url_shortner.service.impl.GeoLookupServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class GeoLookupServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GeoLookupServiceImpl geoLookupService;

    String ipAddress = "8.8.8.8";

    @Test
    void GeoLookupServiceTest_Lookup_ReturnsCountryAndCity() {
        // Arrange
        Map<String, Object> apiResponse = Map.of(
                "status", "success",
                "country", "South Africa",
                "city", "Johannesburg");

        // Stub
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(apiResponse);

        // Act
        GeoInfo result = geoLookupService.lookup(ipAddress);

        // Assert
        assertEquals("South Africa", result.country());
        assertEquals("Johannesburg", result.city());
    }

    @Test
    void GeoLookupServiceTest_Lookup_ReturnsUnknownError_ServiceDown() {
        // Stub
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("API down"));

        // Act
        GeoInfo result = geoLookupService.lookup(ipAddress);

        // Assert
        assertEquals("Unknown", result.country());
        assertEquals("Unknown", result.city());
    }

    @Test
    void GeoLookupServiceTest_Lookup_ReturnsUnknownError() {
        // Act
        GeoInfo result = geoLookupService.lookup(ipAddress);

        // Assert
        assertEquals("Unknown", result.country());
        assertEquals("Unknown", result.city());
    }
}
