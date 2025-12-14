package com.nelani.url_shortner.repository;

import com.nelani.url_shortner.model.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, UUID> {
    @Override
    Page<ShortUrl> findAll(Pageable pageable);

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    boolean existsByOriginalUrl(String originalUrl);

    @Query("""
                SELECT su
                FROM ShortUrl su
                WHERE su.expiresAt IS NOT NULL
                  AND su.expiresAt <= :expiryDate
            """)
    List<ShortUrl> findUrlsExpiredBefore(@Param("expiryDate") LocalDateTime expiryDate);

}
