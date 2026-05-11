package com.example.proman.integration.controller;

import com.example.proman.test.TestSecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TopController 結合テスト")
class TopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("C01: トップ画面 - 認証済みユーザでアクセス")
    void top_authenticatedUser_returnsTopPage() throws Exception {
        mockMvc.perform(get("/")
                        .with(user(TestSecurityUtils.createPmUser())))
                .andExpect(status().isOk())
                .andExpect(view().name("top"))
                .andExpect(model().attributeExists("userName"));
    }

    @Test
    @DisplayName("C02: トップ画面 - 未認証でログインにリダイレクト")
    void top_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
