package com.nelani.url_shortner.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;

@Log4j2
public class UrlShortenerAlgorithm {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String HOSTURL = "https://url-shortener-4yxt.onrender.com";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String encode(String url) {
        log.info("Encoding URL for shortening");

        URI uri;
        try {
            uri = new URI(url);
        } catch (Exception e) {
            log.warn("Validation failed: invalid URI syntax {}", url);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL format.");
        }

        // block redirect loops
        if (uri.toString().toLowerCase().startsWith(HOSTURL.toLowerCase())) {
            log.warn("Blocked self-reference to {}", HOSTURL);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot shorten URLs pointing to this service.");
        }

        // block private IPs (SSRF protection)
        if (isPrivateHost(uri.getHost())) {
            log.warn("Blocked private/internal host: {}", uri.getHost());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "URLs pointing to private/internal networks are not allowed.");
        }

        // Generate a random 8-character short code
        int length = 8;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(BASE62.charAt(RANDOM.nextInt(BASE62.length())));
        }

        String shortCode = sb.toString();
        log.info("Short code generated: {}", shortCode);
        return shortCode;
    }

    public static String buildUrl(String shortCode) {
        String fullUrl = HOSTURL + shortCode;
        log.info("Short URL built successfully");
        return fullUrl;
    }

    public static String decode(String url) {
        String shortCode = url.replace(HOSTURL, "");
        log.info("Short code decoded from URL");
        return shortCode;
    }

    public static String validateUrl(String url) {

        if (url == null || url.isBlank()) {
            log.warn("Validation failed: URL is empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL cannot be empty.");
        }

        url = url.trim();

        // auto-prepend protocol
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        // Enforce maximum URL length
        if (url.length() > 2048) {
            log.warn("Validation failed: URL length {} exceeds maximum allowed", url.length());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "URL is too long. Maximum allowed length is 2048 characters.");
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (Exception e) {
            log.warn("Validation failed: invalid URI syntax {}", url);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL format.");
        }

        if (uri.getHost() == null) {
            log.warn("Validation failed: missing host in {}", url);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL: missing host.");
        }

        // rebuild with lowercase host only
        try {
            url = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost().toLowerCase(),
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment()).toString();
        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL.");
        }

        log.debug("URL validated: {}", url);
        return url;
    }

    private static boolean isPrivateHost(String host) {
        return host.equals("localhost")
                || host.startsWith("127.")
                || host.startsWith("10.")
                || host.startsWith("192.168.")
                || host.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*");
    }

}
