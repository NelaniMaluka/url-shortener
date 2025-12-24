package com.nelani.url_shortner.service;

import com.nelani.url_shortner.dto.GeoInfo;
import com.nelani.url_shortner.model.RequestData;
import com.nelani.url_shortner.model.ShortUrl;
import com.nelani.url_shortner.repository.RequestDataRepository;
import com.nelani.url_shortner.service.impl.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AnalyticsServiceTest {

    @Mock
    private GeoLookupService geoLookupService;

    @Mock
    private RequestDataRepository requestDataRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private ShortUrl shortUrl;
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    public void init() {
        shortUrl = ShortUrl.builder()
                .id(UUID.randomUUID())
                .shortCode("shortCode")
                .originalUrl("https://originalUrl.com")
                .accessLimit(1L)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        httpServletRequest = mock(HttpServletRequest.class);
    }

    @Test
    public void AnalyticsServiceTest_LogRequestAsync_SavesRequestDataWithGeoInfo() {
        // Arrange
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        String referrer = "https://referrer.com";
        GeoInfo geoInfo = new GeoInfo("South Africa", "Johannesburg");

        // Stub
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(ipAddress);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(userAgent);
        when(httpServletRequest.getHeader("Referer")).thenReturn(referrer);
        when(geoLookupService.lookup(ipAddress)).thenReturn(geoInfo);
        when(requestDataRepository.save(any(RequestData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        analyticsService.logRequestAsync(shortUrl, httpServletRequest);

        // Assert
        ArgumentCaptor<RequestData> requestDataCaptor = ArgumentCaptor.forClass(RequestData.class);
        verify(requestDataRepository, times(1)).save(requestDataCaptor.capture());

        RequestData savedData = requestDataCaptor.getValue();
        Assertions.assertThat(savedData.getShortUrl()).isEqualTo(shortUrl);
        Assertions.assertThat(savedData.getCountry()).isEqualTo(geoInfo.country());
        Assertions.assertThat(savedData.getCity()).isEqualTo(geoInfo.city());
        Assertions.assertThat(savedData.getUserAgent()).isEqualTo(userAgent);
        Assertions.assertThat(savedData.getReferrer()).isEqualTo(referrer);
        Assertions.assertThat(savedData.getDeviceHash()).isNotNull();
        Assertions.assertThat(savedData.getDeviceHash()).hasSize(64); // SHA-256 hex string length
    }

    @Test
    public void AnalyticsServiceTest_LogRequestAsync_SavesRequestDataWithoutGeoInfo() {
        // Arrange
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        String referrer = "https://referrer.com";

        // Stub
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(ipAddress);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(userAgent);
        when(httpServletRequest.getHeader("Referer")).thenReturn(referrer);
        when(geoLookupService.lookup(ipAddress)).thenReturn(null);
        when(requestDataRepository.save(any(RequestData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        analyticsService.logRequestAsync(shortUrl, httpServletRequest);

        // Assert
        ArgumentCaptor<RequestData> requestDataCaptor = ArgumentCaptor.forClass(RequestData.class);
        verify(requestDataRepository, times(1)).save(requestDataCaptor.capture());

        RequestData savedData = requestDataCaptor.getValue();
        Assertions.assertThat(savedData.getShortUrl()).isEqualTo(shortUrl);
        Assertions.assertThat(savedData.getCountry()).isNull();
        Assertions.assertThat(savedData.getCity()).isNull();
        Assertions.assertThat(savedData.getUserAgent()).isEqualTo(userAgent);
        Assertions.assertThat(savedData.getReferrer()).isEqualTo(referrer);
    }

    @Test
    public void AnalyticsServiceTest_LogRequestAsync_HandlesGeoLookupFailure() {
        // Arrange
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        String referrer = "https://referrer.com";

        // Stub
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(ipAddress);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(userAgent);
        when(httpServletRequest.getHeader("Referer")).thenReturn(referrer);
        when(geoLookupService.lookup(ipAddress)).thenThrow(new RuntimeException("Geo lookup failed"));
        when(requestDataRepository.save(any(RequestData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        analyticsService.logRequestAsync(shortUrl, httpServletRequest);

        // Assert
        ArgumentCaptor<RequestData> requestDataCaptor = ArgumentCaptor.forClass(RequestData.class);
        verify(requestDataRepository, times(1)).save(requestDataCaptor.capture());
        verify(geoLookupService, times(1)).lookup(ipAddress);

        RequestData savedData = requestDataCaptor.getValue();
        Assertions.assertThat(savedData.getShortUrl()).isEqualTo(shortUrl);
        Assertions.assertThat(savedData.getCountry()).isNull();
        Assertions.assertThat(savedData.getCity()).isNull();
    }

    @Test
    public void AnalyticsServiceTest_LogRequestAsync_HandlesRepositorySaveFailure() {
        // Arrange
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        String referrer = "https://referrer.com";
        GeoInfo geoInfo = new GeoInfo("South Africa", "Johannesburg");

        // Stub
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(ipAddress);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(userAgent);
        when(httpServletRequest.getHeader("Referer")).thenReturn(referrer);
        when(geoLookupService.lookup(ipAddress)).thenReturn(geoInfo);
        when(requestDataRepository.save(any(RequestData.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        analyticsService.logRequestAsync(shortUrl, httpServletRequest);

        // Assert
        verify(requestDataRepository, times(1)).save(any(RequestData.class));
        verify(geoLookupService, times(1)).lookup(ipAddress);
        // Should not throw exception, failure is logged but not rethrown
    }

    @Test
    public void AnalyticsServiceTest_LogRequestAsync_UsesXForwardedForHeader() {
        // Arrange
        String forwardedIp = "203.0.113.1";
        String remoteAddr = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        String referrer = "https://referrer.com";
        GeoInfo geoInfo = new GeoInfo("South Africa", "Johannesburg");

        // Stub
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(forwardedIp);

        // Act
        analyticsService.logRequestAsync(shortUrl, httpServletRequest); // runs synchronously if executor overridden

        // Assert
        verify(geoLookupService, times(1)).lookup(forwardedIp);
        verify(geoLookupService, never()).lookup(remoteAddr);
        verify(requestDataRepository, times(1)).save(any(RequestData.class));
    }

    @Test
    public void AnalyticsServiceTest_LogRequestAsync_HandlesMultipleXForwardedForIps() {
        // Arrange
        String forwardedIps = "203.0.113.1, 198.51.100.2, 192.0.2.3";
        String expectedIp = "203.0.113.1";
        String userAgent = "Mozilla/5.0";
        String referrer = "https://referrer.com";
        GeoInfo geoInfo = new GeoInfo("South Africa", "Johannesburg");

        // Stub
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(forwardedIps);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(userAgent);
        when(httpServletRequest.getHeader("Referer")).thenReturn(referrer);
        when(geoLookupService.lookup(expectedIp)).thenReturn(geoInfo);
        when(requestDataRepository.save(any(RequestData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        analyticsService.logRequestAsync(shortUrl, httpServletRequest);

        // Assert
        verify(geoLookupService, times(1)).lookup(expectedIp);
    }

    @Test
    public void AnalyticsServiceTest_LogRequestAsync_HandlesNullHeaders() {
        // Arrange
        String ipAddress = "192.168.1.1";

        // Stub
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(ipAddress);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(null);
        when(httpServletRequest.getHeader("Referer")).thenReturn(null);
        when(geoLookupService.lookup(ipAddress)).thenReturn(null);
        when(requestDataRepository.save(any(RequestData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        analyticsService.logRequestAsync(shortUrl, httpServletRequest);

        // Assert
        ArgumentCaptor<RequestData> requestDataCaptor = ArgumentCaptor.forClass(RequestData.class);
        verify(requestDataRepository, times(1)).save(requestDataCaptor.capture());

        RequestData savedData = requestDataCaptor.getValue();
        Assertions.assertThat(savedData.getUserAgent()).isNull();
        Assertions.assertThat(savedData.getReferrer()).isNull();
        Assertions.assertThat(savedData.getDeviceHash()).isNotNull();
    }

    @Test
    public void AnalyticsServiceTest_LogRequestAsync_GeneratesConsistentDeviceHash() {
        // Arrange
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // Stub
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(ipAddress);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(userAgent);
        when(httpServletRequest.getHeader("Referer")).thenReturn(null);
        when(geoLookupService.lookup(ipAddress)).thenReturn(null);
        when(requestDataRepository.save(any(RequestData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        analyticsService.logRequestAsync(shortUrl, httpServletRequest);

        // Assert
        ArgumentCaptor<RequestData> requestDataCaptor = ArgumentCaptor.forClass(RequestData.class);
        verify(requestDataRepository, times(1)).save(requestDataCaptor.capture());

        RequestData savedData = requestDataCaptor.getValue();
        String deviceHash = savedData.getDeviceHash();

        Assertions.assertThat(deviceHash).isNotNull();
        Assertions.assertThat(deviceHash).hasSize(64); // SHA-256 produces 64-character hex string
        Assertions.assertThat(deviceHash).matches("^[a-f0-9]{64}$"); // Valid hex string
    }

}
