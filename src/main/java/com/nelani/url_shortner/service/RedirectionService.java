package com.nelani.url_shortner.service;

import jakarta.servlet.http.HttpServletRequest;

public interface RedirectionService {
    String redirect(String shortCode, HttpServletRequest req);
}
