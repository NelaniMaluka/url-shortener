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
                SELECT rd.shortUrl.shortCode AS value,
                       COUNT(rd.id) AS accessCount,
                       COUNT(DISTINCT rd.deviceHash) AS deviceCount
                FROM RequestData rd
                GROUP BY rd.shortUrl.shortCode
            """)
    Page<UrlAccessStats> mostAccessedUrls(Pageable pageable);

    @Query("""
                SELECT rd.country AS value,
                       COUNT(rd.id) AS accessCount,
                       COUNT(DISTINCT rd.deviceHash) AS deviceCount
                FROM RequestData rd
                WHERE rd.country IS NOT NULL
                GROUP BY rd.country
            """)
    Page<UrlAccessStats> mostAccessedCountries(Pageable pageable);

    @Query("""
                SELECT rd.city AS value,
                       COUNT(rd.id) AS accessCount,
                       COUNT(DISTINCT rd.deviceHash) AS deviceCount
                FROM RequestData rd
                WHERE rd.city IS NOT NULL
                GROUP BY rd.city
            """)
    Page<UrlAccessStats> mostAccessedCities(Pageable pageable);

    @Query("""
                SELECT rd.referrer AS value,
                       COUNT(rd.id) AS accessCount,
                       COUNT(DISTINCT rd.deviceHash) AS deviceCount
                FROM RequestData rd
                WHERE rd.referrer IS NOT NULL
                GROUP BY rd.referrer
            """)
    Page<UrlAccessStats> mostAccessedReferrers(Pageable pageable);

    @Query("""
                SELECT rd.userAgent AS value,
                       COUNT(rd.id) AS accessCount,
                       COUNT(DISTINCT rd.deviceHash) AS deviceCount
                FROM RequestData rd
                WHERE rd.userAgent IS NOT NULL
                GROUP BY rd.userAgent
            """)
    Page<UrlAccessStats> mostAccessedUserAgents(Pageable pageable);

    @Query("""
                SELECT FUNCTION('DATE', rd.timestamp) AS value,
                       COUNT(rd.id) AS accessCount,
                       COUNT(DISTINCT rd.deviceHash) AS deviceCount
                FROM RequestData rd
                GROUP BY FUNCTION('DATE', rd.timestamp)
            """)
    Page<UrlAccessStats> accessStatsByDay(Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM RequestData rd WHERE rd.shortUrl = :shortUrl")
    int deleteByShortUrl(@Param("shortUrl") ShortUrl shortUrl);
}
