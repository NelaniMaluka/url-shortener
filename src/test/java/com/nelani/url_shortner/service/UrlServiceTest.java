package com.nelani.url_shortner.service;

import com.nelani.url_shortner.dto.CreateUrlDTO;
import com.nelani.url_shortner.dto.UpdateUrlDTO;
import com.nelani.url_shortner.mapper.UrlResponseMapper;
import com.nelani.url_shortner.model.ShortUrl;
import com.nelani.url_shortner.model.ShortUrlSortField;
import com.nelani.url_shortner.model.SortDirection;
import com.nelani.url_shortner.repository.RequestDataRepository;
import com.nelani.url_shortner.repository.ShortUrlRepository;
import com.nelani.url_shortner.response.UrlResponse;
import com.nelani.url_shortner.service.impl.UrlServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UrlServiceTest {

        @Mock
        private ShortUrlRepository urlRepository;

        @Mock
        private RequestDataRepository requestDataRepository;

        @InjectMocks
        private UrlServiceImpl urlService;

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
        public void UrlServiceTest_ViewAllUrls_ReturnsUrlResponseList() {
                // Arrange
                ShortUrl shortUrl2 = ShortUrl.builder()
                                .id(UUID.randomUUID())
                                .shortCode("shortCode2")
                                .originalUrl("https://originalUrl.com2")
                                .accessLimit(2L)
                                .expiresAt(LocalDateTime.now().plusDays(7))
                                .build();

                Page<ShortUrl> page = new PageImpl<>(
                                List.of(shortUrl, shortUrl2),
                                PageRequest.of(0, 10),
                                1);

                // Stub
                when(urlRepository.findAll(any(Pageable.class))).thenReturn(page);

                // Stub static utility
                try (MockedStatic<UrlResponseMapper> mocked = mockStatic(UrlResponseMapper.class)) {

                        mocked.when(() -> UrlResponseMapper.toDto(
                                        any(ShortUrl.class),
                                        any(RequestDataRepository.class))).thenAnswer(invocation -> {
                                                ShortUrl su = invocation.getArgument(0);

                                                return new UrlResponse(
                                                                su.getOriginalUrl(),
                                                                "TEST_" + su.getShortCode(),
                                                                su.getCreatedAt(),
                                                                su.getUpdatedAt(),
                                                                su.getExpiresAt(),
                                                                0L,
                                                                su.getAccessLimit());
                                        });

                        // Act
                        var result = urlService.viewAllUrls(
                                        0, 10, ShortUrlSortField.ACCESS_LIMIT, SortDirection.DESC);

                        // Assert
                        Assertions.assertThat(result.getTotalElements()).isEqualTo(2);
                        Assertions.assertThat(result).extracting(UrlResponse::shortUrl)
                                        .contains("TEST_" + shortUrl.getShortCode(),
                                                        "TEST_" + shortUrl2.getShortCode());
                        Assertions.assertThat(result)
                                        .extracting(UrlResponse::originalUrl)
                                        .contains(
                                                        shortUrl.getOriginalUrl(),
                                                        shortUrl2.getOriginalUrl());
                        Assertions.assertThat(result)
                                        .extracting(UrlResponse::accessLimit)
                                        .contains(
                                                        shortUrl.getAccessLimit(),
                                                        shortUrl2.getAccessLimit());
                        Assertions.assertThat(result)
                                        .extracting(UrlResponse::expiresAt)
                                        .contains(
                                                        shortUrl.getExpiresAt(),
                                                        shortUrl2.getExpiresAt());
                        Assertions.assertThat(result)
                                        .extracting(UrlResponse::clicks)
                                        .containsOnly(0L);

                }
        }

        @Test
        public void UrlServiceTest_CreateShortUrl_ReturnsUrlResponse() {
                // Arrange
                CreateUrlDTO createUrlDTO = new CreateUrlDTO(shortUrl.getOriginalUrl(), 7, shortUrl.getAccessLimit());

                // Stub
                when(urlRepository.existsByOriginalUrl(anyString())).thenReturn(false);
                when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
                when(requestDataRepository.countByShortUrl(any(ShortUrl.class))).thenReturn(0L);

                // Stub static utility
                try (MockedStatic<UrlResponseMapper> mocked = mockStatic(UrlResponseMapper.class)) {

                        mocked.when(() -> UrlResponseMapper.toDto(
                                        any(ShortUrl.class),
                                        any(Long.class))).thenAnswer(invocation -> {
                                                ShortUrl su = invocation.getArgument(0);

                                                return new UrlResponse(
                                                                su.getOriginalUrl(),
                                                                "TEST_" + su.getShortCode(),
                                                                su.getCreatedAt(),
                                                                su.getUpdatedAt(),
                                                                su.getExpiresAt(),
                                                                0L,
                                                                su.getAccessLimit());
                                        });

                        // Act
                        UrlResponse response = urlService.createShortUrl(createUrlDTO);

                        // Assert
                        verify(urlRepository, times(1)).save(any(ShortUrl.class));
                        Assertions.assertThat(response.shortUrl()).isNotEmpty();
                        Assertions.assertThat(response.originalUrl()).isEqualTo(createUrlDTO.url());
                        Assertions.assertThat(response.createdAt()).isNotNull();
                        Assertions.assertThat(response.accessLimit()).isEqualTo(createUrlDTO.accessLimit());
                        Assertions.assertThat(response.clicks()).isEqualTo(0L);
                        Assertions.assertThat(response.expiresAt()).isNotNull();
                }
        }

        @Test
        public void UrlServiceTest_CreateShortUrl_ReturnsUrlExistsError() {
                // Arrange
                CreateUrlDTO createUrlDTO = new CreateUrlDTO(shortUrl.getOriginalUrl(), 7, shortUrl.getAccessLimit());

                // Stub
                when(urlRepository.existsByOriginalUrl(anyString())).thenReturn(true);

                // Arrange
                assertThatThrownBy(() -> urlService.createShortUrl(createUrlDTO))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Url already exists.");
        }

        @Test
        public void UrlServiceTest_UpdateUrl_ReturnsUrlResponse() {
                // Arrange
                UpdateUrlDTO updateUrlDTO = new UpdateUrlDTO("https://testUrl.com" + shortUrl.getShortCode(),
                                "https://originalUrl.com",
                                30, "newShortKey", 5L);

                // Stub
                when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.of(shortUrl));
                when(urlRepository.existsByOriginalUrl(anyString())).thenReturn(false);
                when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
                when(requestDataRepository.countByShortUrl(any(ShortUrl.class))).thenReturn(0L);

                // Stub static utility
                try (MockedStatic<UrlResponseMapper> mocked = mockStatic(UrlResponseMapper.class)) {

                        mocked.when(() -> UrlResponseMapper.toDto(
                                        any(ShortUrl.class),
                                        any(Long.class))).thenAnswer(invocation -> {
                                                ShortUrl su = invocation.getArgument(0);

                                                return new UrlResponse(
                                                                su.getOriginalUrl(),
                                                                "TEST_" + su.getShortCode(),
                                                                su.getCreatedAt(),
                                                                su.getUpdatedAt(),
                                                                su.getExpiresAt(),
                                                                0L,
                                                                su.getAccessLimit());
                                        });

                        // Act
                        UrlResponse response = urlService.updateUrl(updateUrlDTO);

                        // Assert
                        verify(urlRepository, times(1)).save(any(ShortUrl.class));
                        Assertions.assertThat(response.shortUrl()).isNotEmpty();
                        Assertions.assertThat(response.originalUrl()).isEqualTo(updateUrlDTO.newUrl().toLowerCase());
                        Assertions.assertThat(response.createdAt()).isNotNull();
                        Assertions.assertThat(response.accessLimit()).isEqualTo(updateUrlDTO.accessLimit());
                        Assertions.assertThat(response.clicks()).isEqualTo(0L);
                        Assertions.assertThat(response.expiresAt()).isNotNull();
                }
        }

        @Test
        public void UrlServiceTest_UpdateUrl_ReturnsUrlDoesNotExistsError() {
                // Arrange
                UpdateUrlDTO updateUrlDTO = new UpdateUrlDTO("https://testUrl.com" + shortUrl.getShortCode(),
                                "https://originalUrl.com",
                                30, "newShortKey", 5L);

                // Arrange
                assertThatThrownBy(() -> urlService.updateUrl(updateUrlDTO))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Short url does not exist.");
        }

        @Test
        public void UrlServiceTest_UpdateUrl_ReturnsExistingUrlDoesNotExistsError() {
                // Arrange
                UpdateUrlDTO updateUrlDTO = new UpdateUrlDTO("https://testUrl.com" + shortUrl.getShortCode(),
                                "https://originalUrl.com",
                                30, "newShortKey", 5L);

                // Stub
                when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.of(shortUrl));
                when(urlRepository.existsByOriginalUrl(anyString())).thenReturn(true);

                // Arrange
                assertThatThrownBy(() -> urlService.updateUrl(updateUrlDTO))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Existing url not found.");
        }

        @Test
        public void UrlServiceTest_UpdateUrl_ReturnsUrlExistsError() {
                // Arrange
                UpdateUrlDTO updateUrlDTO = new UpdateUrlDTO("https://testUrl.com" + shortUrl.getShortCode(),
                                "https://originalUrl.com",
                                30, "newShortKey", 5L);
                ShortUrl shortUrl1 = new ShortUrl();

                // Stub
                when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.of(shortUrl));
                when(urlRepository.existsByOriginalUrl(anyString())).thenReturn(true);
                when(urlRepository.findByOriginalUrl(anyString())).thenReturn(Optional.of(shortUrl1));

                // Arrange
                assertThatThrownBy(() -> urlService.updateUrl(updateUrlDTO))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Url already exists.");
        }

        @Test
        public void UrlServiceTest_UpdateUrl_ReturnsShortKeyExistsError() {
                // Arrange
                UpdateUrlDTO updateUrlDTO = new UpdateUrlDTO("https://testUrl.com" + shortUrl.getShortCode(),
                                "https://originalUrl.com",
                                30, "newShortKey", 5L);
                ShortUrl shortUrl1 = new ShortUrl();

                // Stub
                when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.of(shortUrl));
                when(urlRepository.existsByOriginalUrl(anyString())).thenReturn(false);
                when(urlRepository.existsByShortCode(anyString())).thenReturn(true);

                // Assert
                assertThatThrownBy(() -> urlService.updateUrl(updateUrlDTO))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Short Key is already in use.");
        }

        @Test
        public void UrlServiceTest_DeleteUrl_DeletesUrl() {
                // Stub
                when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.of(shortUrl));

                // Act
                urlService.deleteUrl("https://testUrl.com" + shortUrl.getShortCode());

                // Assert
                verify(requestDataRepository, times(1)).deleteByShortUrl(any(ShortUrl.class));
                verify(urlRepository, times(1)).delete(any(ShortUrl.class));
        }

        @Test
        public void UrlServiceTest_DeleteUrl_ReturnsUrlNotFoundError() {
                // Assert
                assertThatThrownBy(() -> urlService.deleteUrl("https://testUrl.com" + shortUrl.getShortCode()))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Short url does not exist.");
        }

}
