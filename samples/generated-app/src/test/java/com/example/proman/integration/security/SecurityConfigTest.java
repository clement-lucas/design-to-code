package com.example.proman.integration.security;

import com.example.proman.test.TestSecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SecurityConfig 認証・認可テスト")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("A01: ログイン画面 - 未認証でもアクセス可能")
    void loginPage_unauthenticated_returns200() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("A02: トップ画面 - 未認証でログインにリダイレクト")
    void topPage_unauthenticated_redirects() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("A03: プロジェクト登録 - 一般ユーザはアクセス拒否")
    @WithMockUser(username = "member", roles = {"USER"})
    void projectCreate_userRole_returns403() throws Exception {
        mockMvc.perform(get("/project/create"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A04: プロジェクト登録 - PMユーザはアクセス可能")
    void projectCreate_pmRole_returns200() throws Exception {
        mockMvc.perform(get("/project/create")
                        .with(user(TestSecurityUtils.createPmUser())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("A05: プロジェクト更新 - 一般ユーザはアクセス拒否")
    @WithMockUser(username = "member", roles = {"USER"})
    void projectUpdate_userRole_returns403() throws Exception {
        mockMvc.perform(get("/project/update")
                        .param("projectId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A06: プロジェクトアップロード - 一般ユーザはアクセス拒否")
    @WithMockUser(username = "member", roles = {"USER"})
    void projectUpload_userRole_returns403() throws Exception {
        mockMvc.perform(get("/project/upload"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A07: プロジェクト検索 - 一般ユーザでアクセス可能")
    void projectSearch_userRole_returns200() throws Exception {
        mockMvc.perform(get("/project/search")
                        .with(user(TestSecurityUtils.createNormalUser())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("A08: 静的リソース - 未認証でもアクセス可能")
    void staticResources_unauthenticated_returns200() throws Exception {
        mockMvc.perform(get("/css/style.css"))
                .andExpect(status().isOk());
    }
}
