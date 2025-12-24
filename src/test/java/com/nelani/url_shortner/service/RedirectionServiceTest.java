package com.nelani.url_shortner.service;

import com.nelani.url_shortner.model.ShortUrl;
import com.nelani.url_shortner.repository.RequestDataRepository;
import com.nelani.url_shortner.repository.ShortUrlRepository;
import com.nelani.url_shortner.service.impl.AnalyticsService;
import com.nelani.url_shortner.service.impl.RedirectionServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RedirectionServiceTest {

        @Mock
        private ShortUrlRepository shortUrlRepository;

        @Mock
        private RequestDataRepository requestDataRepository;

        @Mock
        private AnalyticsService analyticsService;

        @InjectMocks
        private RedirectionServiceImpl redirectionService;

        private ShortUrl shortUrl;

        @BeforeEach
        public void init() {
                shortUrl = ShortUrl.builder()
                                .id(UUID.randomUUID())
                                .shortCode("shortCode")
                                .originalUrl("https://originalUrl.com")
                                .accessLimit(1L)
                                .expiresAt(LocalDateTime.now().plusDays(7))
                                .build();
        }

        @Test
        public void RedirectionServiceTest_Redirect_ReturnsUrl() {
                // Arrange
                HttpServletRequest request = mock(HttpServletRequest.class);

                // Stub
                when(shortUrlRepository.findByShortCode(any(String.class))).thenReturn(Optional.of(shortUrl));
                when(requestDataRepository.countDistinctDeviceHashes(any(UUID.class))).thenReturn(0L);
                doNothing().when(analyticsService).logRequestAsync(any(ShortUrl.class), any(HttpServletRequest.class));

                // Assert
                String result = redirectionService.redirect(shortUrl.getShortCode(), request);
                Assertions.assertThat(result).isEqualTo(shortUrl.getOriginalUrl());
        }

        @Test
        public void RedirectionServiceTest_Redirect_ReturnsNotFoundError() {
                // Arrange
                HttpServletRequest request = mock(HttpServletRequest.class);
                shortUrl.setExpiresAt(LocalDateTime.now().minusDays(30));

                // Assert
                assertThatThrownBy(() -> redirectionService.redirect(shortUrl.getShortCode(), request))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Url does not exist.");
        }

        @Test
        public void RedirectionServiceTest_Redirect_ReturnsExpiredUrlError() {
                // Arrange
                HttpServletRequest request = mock(HttpServletRequest.class);
                shortUrl.setExpiresAt(LocalDateTime.now().minusDays(30));

                // Stub
                when(shortUrlRepository.findByShortCode(any(String.class))).thenReturn(Optional.of(shortUrl));

                // Assert
                assertThatThrownBy(() -> redirectionService.redirect(shortUrl.getShortCode(), request))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Short URL has expired.");
        }

        @Test
        public void RedirectionServiceTest_Redirect_ReturnsLimitExceededError() {
                // Arrange
                HttpServletRequest request = mock(HttpServletRequest.class);

                // Stub
                when(shortUrlRepository.findByShortCode(any(String.class))).thenReturn(Optional.of(shortUrl));
                when(requestDataRepository.countDistinctDeviceHashes(any(UUID.class))).thenReturn(1L);

                // Assert
                assertThatThrownBy(() -> redirectionService.redirect(shortUrl.getShortCode(), request))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining(
                                                "This short URL has reached its maximum number of allowed accesses.");
        }

}
