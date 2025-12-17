package com.nelani.url_shortner.repository;

import com.nelani.url_shortner.dto.UrlAccessStats;
import com.nelani.url_shortner.model.RequestData;
import com.nelani.url_shortner.model.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface RequestDataRepository extends JpaRepository<RequestData, UUID> {

    long countByShortUrl(ShortUrl shortUrl);

    @Query("SELECT COUNT(DISTINCT rd.deviceHash) FROM RequestData rd WHERE rd.shortUrl.id = :shortUrlId")
    long countDistinctDeviceHashes(@Param("shortUrlId") UUID shortUrlId);

    @Query("""
                SELECT
                    rd.shortUrl.shortCode AS shortCode,
                    COUNT(rd.id) AS accessCount
                FROM RequestData rd
                GROUP BY rd.shortUrl.shortCode
                ORDER BY COUNT(rd.id) DESC
            """)
    Page<UrlAccessStats> findMostAccessedUrls(Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM RequestData rd WHERE rd.shortUrl = :shortUrl")
    int deleteByShortUrl(@Param("shortUrl") ShortUrl shortUrl);

}
