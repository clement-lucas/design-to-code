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
@DisplayName("ClientController 結合テスト")
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("C22: 顧客検索画面 - 初期表示")
    void searchInit_authenticated_returnsSearchPage() throws Exception {
        mockMvc.perform(get("/client/search")
                        .with(user(TestSecurityUtils.createPmUser())))
                .andExpect(status().isOk())
                .andExpect(view().name("client/search"))
                .andExpect(model().attributeExists("industryClasses"));
    }

    @Test
    @DisplayName("C23: 顧客検索実行 - 名称指定")
    void searchExecute_withClientName_returnsResults() throws Exception {
        mockMvc.perform(get("/client/search/search")
                        .with(user(TestSecurityUtils.createPmUser()))
                        .param("clientName", "サンプル"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/search"))
                .andExpect(model().attributeExists("clients"))
                .andExpect(model().attribute("searchExecuted", true));
    }
}
