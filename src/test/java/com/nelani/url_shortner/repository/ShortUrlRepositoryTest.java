package com.nelani.url_shortner.repository;

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
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class ShortUrlRepositoryTest {

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    private ShortUrl shortUrl;

    @BeforeEach
    public void init() {
        shortUrl = ShortUrl.builder()
                .shortCode("shortCode")
                .originalUrl("https://originalUrl.com")
                .accessLimit(5L)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    public void ShortUrlRepositoryTest_FindAll_ReturnsPage() {
        // Arrange
        Pageable page = PageRequest.of(0, 10);

        // Act
        shortUrlRepository.save(shortUrl);

        // Assert
        var resultsPage = shortUrlRepository.findAll(page);
        Assertions.assertThat(resultsPage.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(resultsPage.stream().findFirst()).isPresent();

        ShortUrl url = resultsPage.stream().findFirst().get();
        Assertions.assertThat(url.getId()).isEqualTo(shortUrl.getId());
        Assertions.assertThat(url.getShortCode()).isEqualTo(shortUrl.getShortCode());
        Assertions.assertThat(url.getOriginalUrl()).isEqualTo(shortUrl.getOriginalUrl());
        Assertions.assertThat(url.getAccessLimit()).isEqualTo(shortUrl.getAccessLimit());
    }

    @Test
    public void ShortUrlRepositoryTest_FindByShortCode_ReturnsOptionalShortUrl() {
        // Act
        shortUrlRepository.save(shortUrl);

        // Assert
        var result = shortUrlRepository.findByShortCode(shortUrl.getShortCode());
        Assertions.assertThat(result).isPresent();

        ShortUrl url = result.get();
        Assertions.assertThat(url.getId()).isEqualTo(shortUrl.getId());
        Assertions.assertThat(url.getShortCode()).isEqualTo(shortUrl.getShortCode());
        Assertions.assertThat(url.getOriginalUrl()).isEqualTo(shortUrl.getOriginalUrl());
        Assertions.assertThat(url.getAccessLimit()).isEqualTo(shortUrl.getAccessLimit());
    }

    @Test
    public void ShortUrlRepositoryTest_FindByOriginalUrl_ReturnsOptionalShortUrl() {
        // Act
        shortUrlRepository.save(shortUrl);

        // Assert
        var result = shortUrlRepository.findByOriginalUrl(shortUrl.getOriginalUrl());
        Assertions.assertThat(result).isPresent();

        ShortUrl url = result.get();
        Assertions.assertThat(url.getId()).isEqualTo(shortUrl.getId());
        Assertions.assertThat(url.getShortCode()).isEqualTo(shortUrl.getShortCode());
        Assertions.assertThat(url.getOriginalUrl()).isEqualTo(shortUrl.getOriginalUrl());
        Assertions.assertThat(url.getAccessLimit()).isEqualTo(shortUrl.getAccessLimit());
    }

    @Test
    public void ShortUrlRepositoryTest_existsByShortCode_ReturnsTrue() {
        // Act
        shortUrlRepository.save(shortUrl);

        // Assert
        boolean result = shortUrlRepository.existsByShortCode(shortUrl.getShortCode());
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void ShortUrlRepositoryTest_existsByOriginalUrl_ReturnsTrue() {
        // Act
        shortUrlRepository.save(shortUrl);

        // Assert
        boolean result = shortUrlRepository.existsByOriginalUrl(shortUrl.getOriginalUrl());
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void ShortUrlRepositoryTest_FindUrlsExpiredBefore_ReturnsPage() {
        // Arrange
        LocalDateTime cutOffDate = LocalDateTime.now().plusDays(30);

        // Act
        shortUrlRepository.save(shortUrl);

        // Assert
        List<ShortUrl> results = shortUrlRepository.findUrlsExpiredBefore(cutOffDate);
        Assertions.assertThat(results.size()).isEqualTo(1);

        ShortUrl url = results.getFirst();
        Assertions.assertThat(url.getId()).isEqualTo(shortUrl.getId());
        Assertions.assertThat(url.getShortCode()).isEqualTo(shortUrl.getShortCode());
        Assertions.assertThat(url.getOriginalUrl()).isEqualTo(shortUrl.getOriginalUrl());
        Assertions.assertThat(url.getAccessLimit()).isEqualTo(shortUrl.getAccessLimit());
    }

}
