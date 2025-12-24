package com.nelani.url_shortner.service;

import com.nelani.url_shortner.service.impl.UrlShortenerAlgorithm;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UrlShortenerAlgorithmTest {

    @Test
    public void UrlShortenerAlgorithmTest_Encode_ReturnsShortCode() {
        // Arrange
        String validUrl = "https://example.com";

        // Act
        String result = UrlShortenerAlgorithm.encode(validUrl);

        // Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).hasSize(8);
        Assertions.assertThat(result).matches("^[0-9A-Za-z]{8}$");
    }

    @Test
    public void UrlShortenerAlgorithmTest_Encode_ThrowsInvalidUrlFormatError() {
        // Arrange
        String invalidUrl = "https://";

        // Assert
        assertThatThrownBy(() -> UrlShortenerAlgorithm.encode(invalidUrl))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid URL");
    }

    @Test
    public void UrlShortenerAlgorithmTest_Encode_ThrowsSelfReferenceError() {
        // Arrange
        String selfReferenceUrl = "http://localhost:8080/r/abc123";

        // Assert
        assertThatThrownBy(() -> UrlShortenerAlgorithm.encode(selfReferenceUrl))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot shorten URLs pointing to this service.");
    }

    @Test
    public void UrlShortenerAlgorithmTest_Encode_ThrowsPrivateHostError_Localhost() {
        // Arrange
        String localhostUrl = "http://localhost/test";

        // Assert
        assertThatThrownBy(() -> UrlShortenerAlgorithm.encode(localhostUrl))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("URLs pointing to private/internal networks are not allowed.");
    }

    @Test
    public void UrlShortenerAlgorithmTest_Encode_ThrowsPrivateHostError_127() {
        // Arrange
        String privateIpUrl = "http://127.0.0.1/test";

        // Assert
        assertThatThrownBy(() -> UrlShortenerAlgorithm.encode(privateIpUrl))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("URLs pointing to private/internal networks are not allowed.");
    }

    @Test
    public void UrlShortenerAlgorithmTest_Encode_ThrowsPrivateHostError_192_168() {
        // Arrange
        String privateIpUrl = "http://192.168.1.1/test";

        // Assert
        assertThatThrownBy(() -> UrlShortenerAlgorithm.encode(privateIpUrl))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("URLs pointing to private/internal networks are not allowed.");
    }

    @Test
    public void UrlShortenerAlgorithmTest_Encode_ThrowsPrivateHostError_10() {
        // Arrange
        String privateIpUrl = "http://10.0.0.1/test";

        // Assert
        assertThatThrownBy(() -> UrlShortenerAlgorithm.encode(privateIpUrl))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("URLs pointing to private/internal networks are not allowed.");
    }

    @Test
    public void UrlShortenerAlgorithmTest_Encode_ThrowsPrivateHostError_172() {
        // Arrange
        String privateIpUrl = "http://172.16.0.1/test";

        // Assert
        assertThatThrownBy(() -> UrlShortenerAlgorithm.encode(privateIpUrl))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("URLs pointing to private/internal networks are not allowed.");
    }

    @Test
    public void UrlShortenerAlgorithmTest_BuildUrl_ReturnsFullUrl() {
        // Arrange
        String shortCode = "abc12345";

        // Act
        String result = UrlShortenerAlgorithm.buildUrl(shortCode);

        // Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isNotEmpty();
    }

    @Test
    public void UrlShortenerAlgorithmTest_Decode_ReturnsShortCode() {
        // Arrange
        String fullUrl = "http://localhost:8080/r/abc12345";
        String expectedShortCode = "abc12345";

        // Act
        String result = UrlShortenerAlgorithm.decode(fullUrl);

        // Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isNotEmpty();
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_ReturnsValidatedUrl() {
        // Arrange
        String url = "https://example.com/path?query=value";

        // Act
        String result = UrlShortenerAlgorithm.validateUrl(url);

        // Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo(url.toLowerCase());
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_ThrowsEmptyUrlError() {
        // Arrange
        String emptyUrl = "";

        // Assert
        assertThatThrownBy(() -> UrlShortenerAlgorithm.validateUrl(emptyUrl))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("URL cannot be empty.");
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_ThrowsNullUrlError() {
        // Assert
        assertThatThrownBy(() -> UrlShortenerAlgorithm.validateUrl(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("URL cannot be empty.");
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_ThrowsBlankUrlError() {
        // Arrange
        String blankUrl = "   ";

        // Assert
        assertThatThrownBy(() -> UrlShortenerAlgorithm.validateUrl(blankUrl))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("URL cannot be empty.");
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_PrependsHttpsProtocol() {
        // Arrange
        String urlWithoutProtocol = "example.com/path";

        // Act
        String result = UrlShortenerAlgorithm.validateUrl(urlWithoutProtocol);

        // Assert
        Assertions.assertThat(result).startsWith("https://");
        Assertions.assertThat(result).contains("example.com");
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_PreservesHttpProtocol() {
        // Arrange
        String httpUrl = "http://example.com/path";

        // Act
        String result = UrlShortenerAlgorithm.validateUrl(httpUrl);

        // Assert
        Assertions.assertThat(result).startsWith("http://");
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_TrimsWhitespace() {
        // Arrange
        String urlWithWhitespace = "  https://example.com  ";

        // Act
        String result = UrlShortenerAlgorithm.validateUrl(urlWithWhitespace);

        // Assert
        Assertions.assertThat(result).doesNotStartWith(" ");
        Assertions.assertThat(result).doesNotEndWith(" ");
        Assertions.assertThat(result).contains("example.com");
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_ThrowsUrlTooLongError() {
        // Arrange
        String longUrl = "https://example.com/" + "a".repeat(2050);

        // Assert
        assertThatThrownBy(() -> UrlShortenerAlgorithm.validateUrl(longUrl))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("URL is too long. Maximum allowed length is 2048 characters.");
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_ThrowsInvalidUrlFormatError() {
        // Arrange
        String invalidUrl = "not a valid url format";

        // Assert
        assertThatThrownBy(() -> UrlShortenerAlgorithm.validateUrl(invalidUrl))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid URL format.");
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_ConvertsHostToLowercase() {
        // Arrange
        String urlWithUppercaseHost = "https://EXAMPLE.COM/path";

        // Act
        String result = UrlShortenerAlgorithm.validateUrl(urlWithUppercaseHost);

        // Assert
        Assertions.assertThat(result).contains("example.com");
        Assertions.assertThat(result).doesNotContain("EXAMPLE.COM");
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_HandlesUrlWithPort() {
        // Arrange
        String urlWithPort = "https://example.com:8080/path";

        // Act
        String result = UrlShortenerAlgorithm.validateUrl(urlWithPort);

        // Assert
        Assertions.assertThat(result).contains("example.com:8080");
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_HandlesUrlWithQueryParams() {
        // Arrange
        String urlWithQuery = "https://example.com/path?param1=value1&param2=value2";

        // Act
        String result = UrlShortenerAlgorithm.validateUrl(urlWithQuery);

        // Assert
        Assertions.assertThat(result).contains("param1=value1");
        Assertions.assertThat(result).contains("param2=value2");
    }

    @Test
    public void UrlShortenerAlgorithmTest_ValidateUrl_HandlesUrlWithFragment() {
        // Arrange
        String urlWithFragment = "https://example.com/path#section";

        // Act
        String result = UrlShortenerAlgorithm.validateUrl(urlWithFragment);

        // Assert
        Assertions.assertThat(result).contains("#section");
    }

}
