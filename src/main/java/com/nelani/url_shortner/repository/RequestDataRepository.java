package com.nelani.url_shortner.repository;

import com.nelani.url_shortner.dto.UrlAccessStats;
import com.nelani.url_shortner.model.RequestData;
import com.nelani.url_shortner.model.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RequestDataRepository extends JpaRepository<RequestData, UUID> {

    int countByShortUrl(ShortUrl shortUrl);

    @Query("""
                SELECT
                    rd.shortUrl.shortCode AS shortCode,
                    COUNT(rd.id) AS accessCount
                FROM RequestData rd
                GROUP BY rd.shortUrl.shortCode
                ORDER BY COUNT(rd.id) DESC
            """)
    Page<UrlAccessStats> findMostAccessedUrls(Pageable pageable);

}
