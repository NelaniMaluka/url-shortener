package com.nelani.url_shortner.controller;

import com.nelani.url_shortner.dto.UrlAccessStats;
import com.nelani.url_shortner.dto.UrlAccessStatsDTO;
import com.nelani.url_shortner.model.SortDirection;
import com.nelani.url_shortner.model.StatsGroupBy;
import com.nelani.url_shortner.service.RequestDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RequestDataController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RequestDataControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private RequestDataService requestDataService;

        @Test
        public void RequestDataController_Stats_ReturnsStatsPageJson() throws Exception {
                // Arrange
                UrlAccessStats stat1 = new UrlAccessStatsDTO("value1", 10L, 5L);
                UrlAccessStats stat2 = new UrlAccessStatsDTO("value2", 3L, 2L);

                Page<UrlAccessStats> page = new PageImpl<>(
                                List.of(stat1, stat2),
                                PageRequest.of(0, 10),
                                2);

                // Stub
                when(requestDataService.getTopStats(
                                any(StatsGroupBy.class),
                                anyInt(),
                                anyInt(),
                                any(SortDirection.class))).thenReturn(page);

                // Act & Assert
                mockMvc.perform(get("/api/request-data/stats")
                                .param("groupBy", "COUNTRY")
                                .param("page", "0")
                                .param("size", "10")
                                .param("direction", "DESC")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content.length()").value(2))
                                .andExpect(jsonPath("$.content[0].value").value("value1"))
                                .andExpect(jsonPath("$.content[0].accessCount").value(10))
                                .andExpect(jsonPath("$.content[0].deviceCount").value(5))
                                .andExpect(jsonPath("$.content[1].value").value("value2"))
                                .andExpect(jsonPath("$.content[1].accessCount").value(3))
                                .andExpect(jsonPath("$.content[1].deviceCount").value(2));
        }
}
