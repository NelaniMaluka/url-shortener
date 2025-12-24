package com.nelani.url_shortner.service;

import com.nelani.url_shortner.dto.UrlAccessStats;
import com.nelani.url_shortner.dto.UrlAccessStatsDTO;
import com.nelani.url_shortner.model.SortDirection;
import com.nelani.url_shortner.model.StatsGroupBy;
import com.nelani.url_shortner.repository.RequestDataRepository;
import com.nelani.url_shortner.service.impl.RequestDataServiceImpl;
import com.nelani.url_shortner.service.impl.UrlShortenerAlgorithm;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RequestDataServiceTest {

        @Mock
        private RequestDataRepository requestDataRepository;

        @InjectMocks
        private RequestDataServiceImpl requestDataService;

        @Test
        public void RequestDataServiceTest_GetTopStats_ReturnsUrlAccessStats() {
                // Arrange
                UrlAccessStats urlAccessStats = new UrlAccessStatsDTO("ShortCode", 2L, 1L);
                UrlAccessStats urlAccessStats2 = new UrlAccessStatsDTO("ShortCode2", 1L, 1L);
                Page<UrlAccessStats> page = new PageImpl<>(
                                List.of(urlAccessStats, urlAccessStats2),
                                PageRequest.of(0, 10),
                                1);

                // Stub
                when(requestDataRepository.mostAccessedUrls(any(Pageable.class)))
                                .thenReturn(page);

                // Stub static utility
                try (MockedStatic<UrlShortenerAlgorithm> mocked = mockStatic(UrlShortenerAlgorithm.class)) {

                        mocked.when(() -> UrlShortenerAlgorithm.buildUrl(anyString()))
                                        .thenAnswer(invocation -> "TEST_" + invocation.getArgument(0));

                        // Assert
                        var result = requestDataService.getTopStats(StatsGroupBy.URL, 0, 10, SortDirection.DESC);
                        Assertions.assertThat(result.getTotalElements()).isEqualTo(2);
                        Assertions.assertThat(result).extracting(UrlAccessStats::getValue).contains(
                                        "TEST_" + urlAccessStats.getValue(),
                                        "TEST_" + urlAccessStats2.getValue());
                        Assertions.assertThat(result).extracting(UrlAccessStats::getAccessCount).contains(1L, 2L);
                        Assertions.assertThat(result).extracting(UrlAccessStats::getDeviceCount).contains(1L, 1L);
                }
        }

        @Test
        public void RequestDataServiceTest_GetTopStats_ReturnsCountryAccessStats() {
                // Arrange
                UrlAccessStats urlAccessStats = new UrlAccessStatsDTO("country1", 2L, 1L);
                UrlAccessStats urlAccessStats2 = new UrlAccessStatsDTO("country2", 1L, 1L);
                Page<UrlAccessStats> page = new PageImpl<>(
                                List.of(urlAccessStats, urlAccessStats2),
                                PageRequest.of(0, 10),
                                1);

                // Stub
                when(requestDataRepository.mostAccessedCountries(any(Pageable.class)))
                                .thenReturn(page);

                // Assert
                var result = requestDataService.getTopStats(StatsGroupBy.COUNTRY, 0, 10, SortDirection.DESC);
                Assertions.assertThat(result.getTotalElements()).isEqualTo(2);
                Assertions.assertThat(result).extracting(UrlAccessStats::getValue).contains(urlAccessStats.getValue(),
                                urlAccessStats2.getValue());
                Assertions.assertThat(result).extracting(UrlAccessStats::getAccessCount).contains(1L, 2L);
                Assertions.assertThat(result).extracting(UrlAccessStats::getDeviceCount).contains(1L, 1L);
        }

        @Test
        public void RequestDataServiceTest_GetTopStats_ReturnsCityAccessStats() {
                // Arrange
                UrlAccessStats urlAccessStats = new UrlAccessStatsDTO("city1", 2L, 1L);
                UrlAccessStats urlAccessStats2 = new UrlAccessStatsDTO("city2", 1L, 1L);
                Page<UrlAccessStats> page = new PageImpl<>(
                                List.of(urlAccessStats, urlAccessStats2),
                                PageRequest.of(0, 10),
                                1);

                // Stub
                when(requestDataRepository.mostAccessedCities(any(Pageable.class)))
                                .thenReturn(page);

                // Assert
                var result = requestDataService.getTopStats(StatsGroupBy.CITY, 0, 10, SortDirection.DESC);
                Assertions.assertThat(result.getTotalElements()).isEqualTo(2);
                Assertions.assertThat(result).extracting(UrlAccessStats::getValue).contains(urlAccessStats.getValue(),
                                urlAccessStats2.getValue());
                Assertions.assertThat(result).extracting(UrlAccessStats::getAccessCount).contains(1L, 2L);
                Assertions.assertThat(result).extracting(UrlAccessStats::getDeviceCount).contains(1L, 1L);
        }

        @Test
        public void RequestDataServiceTest_GetTopStats_ReturnsUserAgentAccessStats() {
                // Arrange
                UrlAccessStats urlAccessStats = new UrlAccessStatsDTO("agent1", 2L, 1L);
                UrlAccessStats urlAccessStats2 = new UrlAccessStatsDTO("agent2", 1L, 1L);
                Page<UrlAccessStats> page = new PageImpl<>(
                                List.of(urlAccessStats, urlAccessStats2),
                                PageRequest.of(0, 10),
                                1);

                // Stub
                when(requestDataRepository.mostAccessedUserAgents(any(Pageable.class)))
                                .thenReturn(page);

                // Assert
                var result = requestDataService.getTopStats(StatsGroupBy.USER_AGENT, 0, 10, SortDirection.DESC);
                Assertions.assertThat(result.getTotalElements()).isEqualTo(2);
                Assertions.assertThat(result).extracting(UrlAccessStats::getValue).contains(urlAccessStats.getValue(),
                                urlAccessStats2.getValue());
                Assertions.assertThat(result).extracting(UrlAccessStats::getAccessCount).contains(1L, 2L);
                Assertions.assertThat(result).extracting(UrlAccessStats::getDeviceCount).contains(1L, 1L);
        }

}
