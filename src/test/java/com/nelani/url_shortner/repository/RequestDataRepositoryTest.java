package com.nelani.url_shortner.repository;

import com.nelani.url_shortner.dto.UrlAccessStats;
import com.nelani.url_shortner.model.RequestData;
import com.nelani.url_shortner.model.ShortUrl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class RequestDataRepositoryTest {

    @Autowired
    private RequestDataRepository requestDataRepository;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    private RequestData requestData;
    private ShortUrl shortUrl;

    @BeforeEach
    public void init() {
        shortUrl = ShortUrl.builder()
                .shortCode("shortCode")
                .originalUrl("https://originalUrl.com")
                .accessLimit(5L)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        ShortUrl shortUrl2 = ShortUrl.builder()
                .shortCode("shortCode2")
                .originalUrl("https://originalUrl.com2")
                .accessLimit(5L)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        requestData = RequestData.builder()
                .deviceHash("deviceHash")
                .userAgent("userAgent")
                .referrer("referrer")
                .shortUrl(shortUrl)
                .city("city")
                .country("country")
                .build();

        RequestData requestData2 = RequestData.builder()
                .deviceHash("deviceHash")
                .userAgent("userAgent2")
                .referrer("referrer2")
                .shortUrl(shortUrl2)
                .city("city2")
                .country("country2")
                .build();

        shortUrlRepository.save(shortUrl);
        shortUrlRepository.save(shortUrl2);
        requestDataRepository.save(requestData2);
    }

    @Test
    public void RequestDataRepositoryTest_CountByShortUrl_ReturnsLong() {
        // Act
        requestDataRepository.save(requestData);

        // Assert
        long count = requestDataRepository.countByShortUrl(shortUrl);
        Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    public void RequestDataRepositoryTest_CountDistinctDeviceHashes_ReturnsLong() {
        // Arrange
        RequestData requestData2 = RequestData.builder()
                .deviceHash("deviceHash")
                .userAgent("userAgent2")
                .referrer("referrer2")
                .shortUrl(shortUrl)
                .city("city2")
                .country("country2")
                .build();

        // Act
        requestDataRepository.save(requestData);
        requestDataRepository.save(requestData2);

        // Assert
        long count = requestDataRepository.countDistinctDeviceHashes(shortUrl.getId());
        Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    public void RequestDataRepositoryTest_MostAccessedUrls_ReturnsLong() {
        // Arrange
        Sort sort = Sort.by(Sort.Direction.ASC, "accessCount");
        Pageable page = PageRequest.of(0, 10, sort);
        RequestData requestData2 = RequestData.builder()
                .deviceHash("deviceHash")
                .userAgent("userAgent2")
                .referrer("referrer2")
                .shortUrl(shortUrl)
                .city("city2")
                .country("country2")
                .build();

        // Act
        requestDataRepository.save(requestData);
        requestDataRepository.save(requestData2);

        // Assert
        var results = requestDataRepository.mostAccessedUrls(page);
        Assertions.assertThat(results.getTotalElements()).isEqualTo(2);
        Assertions.assertThat(results).extracting(UrlAccessStats::getValue).contains(shortUrl.getShortCode(),
                "shortCode2");
        Assertions.assertThat(results).extracting(UrlAccessStats::getAccessCount).contains(1L, 2L);
        Assertions.assertThat(results).extracting(UrlAccessStats::getDeviceCount).contains(1L, 1L);
    }

    @Test
    public void RequestDataRepositoryTest_MostAccessedCountries_ReturnsLong() {
        // Arrange
        Sort sort = Sort.by(Sort.Direction.ASC, "accessCount");
        Pageable page = PageRequest.of(0, 10, sort);
        RequestData requestData2 = RequestData.builder()
                .deviceHash("deviceHash")
                .userAgent("userAgent2")
                .referrer("referrer2")
                .shortUrl(shortUrl)
                .city("city2")
                .country("country2")
                .build();

        // Act
        requestDataRepository.save(requestData);
        requestDataRepository.save(requestData2);

        // Assert
        var results = requestDataRepository.mostAccessedCountries(page);
        Assertions.assertThat(results.getTotalElements()).isEqualTo(2);
        Assertions.assertThat(results).extracting(UrlAccessStats::getValue).contains(requestData.getCountry(),
                requestData2.getCountry());
        Assertions.assertThat(results).extracting(UrlAccessStats::getAccessCount).contains(1L, 2L);
        Assertions.assertThat(results).extracting(UrlAccessStats::getDeviceCount).contains(1L, 1L);
    }

    @Test
    public void RequestDataRepositoryTest_MostAccessedCities_ReturnsLong() {
        // Arrange
        Sort sort = Sort.by(Sort.Direction.ASC, "accessCount");
        Pageable page = PageRequest.of(0, 10, sort);
        RequestData requestData2 = RequestData.builder()
                .deviceHash("deviceHash")
                .userAgent("userAgent2")
                .referrer("referrer2")
                .shortUrl(shortUrl)
                .city("city2")
                .country("country2")
                .build();

        // Act
        requestDataRepository.save(requestData);
        requestDataRepository.save(requestData2);

        // Assert
        var results = requestDataRepository.mostAccessedCities(page);
        Assertions.assertThat(results.getTotalElements()).isEqualTo(2);
        Assertions.assertThat(results).extracting(UrlAccessStats::getValue).contains(requestData.getCity(),
                requestData2.getCity());
        Assertions.assertThat(results).extracting(UrlAccessStats::getAccessCount).contains(1L, 2L);
        Assertions.assertThat(results).extracting(UrlAccessStats::getDeviceCount).contains(1L, 1L);
    }

    @Test
    public void RequestDataRepositoryTest_MostAccessedUserAgents_ReturnsLong() {
        // Arrange
        Sort sort = Sort.by(Sort.Direction.ASC, "accessCount");
        Pageable page = PageRequest.of(0, 10, sort);
        RequestData requestData2 = RequestData.builder()
                .deviceHash("deviceHash")
                .userAgent("userAgent2")
                .referrer("referrer2")
                .shortUrl(shortUrl)
                .city("city2")
                .country("country2")
                .build();

        // Act
        requestDataRepository.save(requestData);
        requestDataRepository.save(requestData2);

        // Assert
        var results = requestDataRepository.mostAccessedUserAgents(page);
        Assertions.assertThat(results.getTotalElements()).isEqualTo(2);
        Assertions.assertThat(results).extracting(UrlAccessStats::getValue).contains(requestData.getUserAgent(),
                requestData2.getUserAgent());
        Assertions.assertThat(results).extracting(UrlAccessStats::getAccessCount).contains(1L, 2L);
        Assertions.assertThat(results).extracting(UrlAccessStats::getDeviceCount).contains(1L, 1L);
    }

    @Test
    public void RequestDataRepositoryTest_DeleteByShortUrl_ReturnsLong() {
        // Act
        requestDataRepository.save(requestData);

        // Assert
        long result = requestDataRepository.deleteByShortUrl(shortUrl);
        Assertions.assertThat(result).isEqualTo(1);
    }

}
