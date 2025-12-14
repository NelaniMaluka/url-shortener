package com.nelani.url_shortner.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 8, nullable = false, unique = true)
    @NotBlank(message = "Short code cannot be null or blank")
    @Size(min = 8, max = 8, message = "Short code must be 8 characters long")
    private String shortCode;

    @Lob
    @Column(nullable = false)
    @URL(message = "Invalid URL format")
    @NotBlank(message = "Original URL cannot be null or blank")
    @Size(max = 2048, message = "Original URL is too long. Maximum allowed length is 2048 characters.")
    private String originalUrl;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private LocalDateTime expiresAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
