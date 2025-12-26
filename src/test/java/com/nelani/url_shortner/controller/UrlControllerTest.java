package com.nelani.url_shortner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nelani.url_shortner.dto.CreateUrlDTO;
import com.nelani.url_shortner.dto.UpdateUrlDTO;
import com.nelani.url_shortner.model.ShortUrlSortField;
import com.nelani.url_shortner.model.SortDirection;
import com.nelani.url_shortner.response.UrlResponse;
import com.nelani.url_shortner.service.UrlService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UrlController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UrlControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UrlService urlService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Test
        public void UrlController_ViewAllUrls_ReturnsPageJson() throws Exception {
                // Arrange
                UrlResponse url1 = new UrlResponse(
                                "https://original1.com",
                                "http://localhost:8080/r/abc12345",
                                LocalDateTime.now(),
                                null,
                                LocalDateTime.now().plusDays(7),
                                5L,
                                10L);

                UrlResponse url2 = new UrlResponse(
                                "https://original2.com",
                                "http://localhost:8080/r/xyz67890",
                                LocalDateTime.now(),
                                null,
                                LocalDateTime.now().plusDays(30),
                                2L,
                                20L);

                Page<UrlResponse> page = new PageImpl<>(
                                List.of(url1, url2),
                                PageRequest.of(0, 10),
                                2);

                // Stub
                when(urlService.viewAllUrls(
                                anyInt(),
                                anyInt(),
                                any(ShortUrlSortField.class),
                                any(SortDirection.class))).thenReturn(page);

                // Act & Assert
                mockMvc.perform(get("/api/urls")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "CREATED_AT")
                                .param("direction", "DESC"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content.length()").value(2))
                                .andExpect(jsonPath("$.content[0].originalUrl").value("https://original1.com"))
                                .andExpect(jsonPath("$.content[1].originalUrl").value("https://original2.com"));
        }

        @Test
        public void UrlController_AddUrlsBulk_ReturnsBulkResultsJson() throws Exception {
                // Arrange
                CreateUrlDTO dto1 = new CreateUrlDTO("https://original1.com", 7, 10L);
                CreateUrlDTO dto2 = new CreateUrlDTO("https://original2.com", 30, 20L);

                UrlResponse response1 = new UrlResponse(
                                dto1.url(),
                                "http://localhost:8080/r/abc12345",
                                LocalDateTime.now(),
                                null,
                                LocalDateTime.now().plusDays(7),
                                0L,
                                dto1.accessLimit());

                UrlResponse response2 = new UrlResponse(
                                dto2.url(),
                                "http://localhost:8080/r/xyz67890",
                                LocalDateTime.now(),
                                null,
                                LocalDateTime.now().plusDays(30),
                                0L,
                                dto2.accessLimit());

                // Stub
                when(urlService.createShortUrl(dto1)).thenReturn(response1);
                when(urlService.createShortUrl(dto2)).thenReturn(response2);

                String requestJson = objectMapper.writeValueAsString(List.of(dto1, dto2));

                // Act & Assert
                mockMvc.perform(post("/api/urls/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].request.url").value(dto1.url()))
                                .andExpect(jsonPath("$[0].response.shortUrl").value(response1.shortUrl()))
                                .andExpect(jsonPath("$[0].error").doesNotExist())
                                .andExpect(jsonPath("$[1].request.url").value(dto2.url()))
                                .andExpect(jsonPath("$[1].response.shortUrl").value(response2.shortUrl()))
                                .andExpect(jsonPath("$[1].error").doesNotExist());
        }

        @Test
        public void UrlController_UpdateUrl_ReturnsUrlResponseJson() throws Exception {
                // Arrange
                UpdateUrlDTO dto = new UpdateUrlDTO(
                                "http://localhost:8080/r/abc12345",
                                "https://updated.com",
                                30,
                                "newKey",
                                5L);

                UrlResponse response = new UrlResponse(
                                dto.newUrl(),
                                "http://localhost:8080/r/newKey",
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                LocalDateTime.now().plusDays(30),
                                0L,
                                dto.accessLimit());

                // Stub
                when(urlService.updateUrl(dto)).thenReturn(response);

                String requestJson = objectMapper.writeValueAsString(dto);

                // Act & Assert
                mockMvc.perform(put("/api/urls/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.originalUrl").value(dto.newUrl()))
                                .andExpect(jsonPath("$.shortUrl").value(response.shortUrl()))
                                .andExpect(jsonPath("$.accessLimit").value(dto.accessLimit()));
        }

        @Test
        public void UrlController_RemoveUrl_ReturnsSuccessMessage() throws Exception {
                // Arrange
                String urlToDelete = "http://localhost:8080/r/abc12345";

                // Stub
                doNothing().when(urlService).deleteUrl(urlToDelete);

                // Act & Assert
                mockMvc.perform(delete("/api/urls/delete")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("\"" + urlToDelete + "\""))
                                .andExpect(status().isNoContent());
        }
}
