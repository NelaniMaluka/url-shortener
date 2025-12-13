package com.nelani.url_shortner.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "short_url_id", nullable = false)
    private ShortUrl shortUrl;

    @Column(length = 45, nullable = false) // IPv6 max length
    @NotBlank(message = "IP address cannot be blank")
    private String ipAddress;

    @Column(length = 100)
    @Size(max = 100, message = "Country name is too long")
    private String country;

    @Column(length = 100)
    @Size(max = 100, message = "City name is too long")
    private String city;

    @Column(length = 2048)
    @Size(max = 2048, message = "Referrer URL is too long")
    private String referrer;

    @Column(length = 512)
    @Size(max = 512, message = "User agent string is too long")
    private String userAgent;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

}
