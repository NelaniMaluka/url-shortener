package com.nelani.url_shortner.controller;

import com.nelani.url_shortner.service.RedirectionService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RedirectionController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RedirectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RedirectionService redirectionService;

    @Test
    public void RedirectionController_Redirect_Returns302WithLocationHeader() throws Exception {
        // Arrange
        String shortCode = "abc12345";
        String longUrl = "https://original-url.com/path";

        // Stub
        when(redirectionService.redirect(anyString(), any(HttpServletRequest.class)))
                .thenReturn(longUrl);

        // Act & Assert
        mockMvc.perform(get("/r/{shortCode}", shortCode)
                .accept(MediaType.ALL))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", longUrl));
    }
}
