package com.example.proman.system.functional;

import com.example.proman.security.LoginUserDetails;
import com.example.proman.test.TestSecurityUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("プロジェクト管理 機能テスト")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProjectFlowTest {

    @Autowired
    private MockMvc mockMvc;

    private final LoginUserDetails pmUser = TestSecurityUtils.createPmUser();

    @Test
    @Order(1)
    @DisplayName("F01: プロジェクト登録フロー - 入力→確認→実行→完了")
    void projectCreateFlow() throws Exception {
        // Step 1: 登録画面表示
        mockMvc.perform(get("/project/create")
                        .with(user(pmUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/create"));

        // Step 2: 確認画面へ
        mockMvc.perform(post("/project/create/confirm")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectName", "機能テスト用プロジェクト")
                        .param("projectType", "01")
                        .param("projectClass", "A")
                        .param("projectStartDate", "2024/04/01")
                        .param("projectEndDate", "2025/03/31")
                        .param("organizationId", "2")
                        .param("clientId", "1")
                        .param("projectManager", "山田太郎")
                        .param("projectLeader", "田中花子"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/confirmCreate"));

        // Step 3: 実行→リダイレクト
        mockMvc.perform(post("/project/create/execute")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectName", "機能テスト用プロジェクト")
                        .param("projectType", "01")
                        .param("projectClass", "A")
                        .param("projectStartDate", "2024/04/01")
                        .param("projectEndDate", "2025/03/31")
                        .param("organizationId", "2")
                        .param("clientId", "1")
                        .param("projectManager", "山田太郎")
                        .param("projectLeader", "田中花子"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/project/create/complete"));

        // Step 4: 完了画面
        mockMvc.perform(get("/project/create/complete")
                        .with(user(pmUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/completeCreate"));
    }

    @Test
    @Order(2)
    @DisplayName("F02: プロジェクト登録フロー(戻る) - 入力→確認→戻る")
    void projectCreateBackFlow() throws Exception {
        // 確認画面へ
        mockMvc.perform(post("/project/create/confirm")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectName", "戻るテスト")
                        .param("projectType", "02")
                        .param("projectClass", "S")
                        .param("projectStartDate", "2024/04/01")
                        .param("projectEndDate", "2025/03/31")
                        .param("organizationId", "1")
                        .param("clientId", "1")
                        .param("projectManager", "山田太郎")
                        .param("projectLeader", "田中花子"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/confirmCreate"));

        // 戻るボタン
        mockMvc.perform(post("/project/create/execute")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectName", "戻るテスト")
                        .param("projectType", "02")
                        .param("projectClass", "S")
                        .param("projectStartDate", "2024/04/01")
                        .param("projectEndDate", "2025/03/31")
                        .param("organizationId", "1")
                        .param("clientId", "1")
                        .param("projectManager", "山田太郎")
                        .param("projectLeader", "田中花子")
                        .param("back", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("project/create"));
    }

    @Test
    @Order(3)
    @DisplayName("F03: プロジェクト検索フロー")
    void projectSearchFlow() throws Exception {
        // 検索画面表示
        mockMvc.perform(get("/project/search")
                        .with(user(pmUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/search"));

        // 検索実行
        mockMvc.perform(get("/project/search/search")
                        .with(user(pmUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/search"))
                .andExpect(model().attributeExists("searchExecuted"));
    }

    @Test
    @Order(4)
    @DisplayName("F04: プロジェクト更新フロー - 詳細→更新→確認→実行→完了")
    void projectUpdateFlow() throws Exception {
        // 詳細画面
        mockMvc.perform(get("/project/detail")
                        .with(user(pmUser))
                        .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/detail"));

        // 更新画面
        mockMvc.perform(get("/project/update")
                        .with(user(pmUser))
                        .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/update"));

        // 更新確認
        mockMvc.perform(post("/project/update/confirm")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectId", "1")
                        .param("projectName", "更新テストプロジェクト1")
                        .param("projectType", "01")
                        .param("projectClass", "A")
                        .param("projectStartDate", "2024/01/01")
                        .param("projectEndDate", "2024/12/31")
                        .param("organizationId", "2")
                        .param("clientId", "1")
                        .param("projectManager", "山田太郎")
                        .param("projectLeader", "田中花子")
                        .param("versionNo", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/confirmUpdate"));

        // 更新実行
        mockMvc.perform(post("/project/update/execute")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectId", "1")
                        .param("projectName", "更新テストプロジェクト1")
                        .param("projectType", "01")
                        .param("projectClass", "A")
                        .param("projectStartDate", "2024/01/01")
                        .param("projectEndDate", "2024/12/31")
                        .param("organizationId", "2")
                        .param("clientId", "1")
                        .param("projectManager", "山田太郎")
                        .param("projectLeader", "田中花子")
                        .param("versionNo", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/project/update/complete"));

        // 完了画面
        mockMvc.perform(get("/project/update/complete")
                        .with(user(pmUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/completeUpdate"));
    }

    @Test
    @Order(5)
    @DisplayName("F05: プロジェクト詳細表示")
    void projectDetailView() throws Exception {
        mockMvc.perform(get("/project/detail")
                        .with(user(pmUser))
                        .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/detail"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("projectTypeName"))
                .andExpect(model().attributeExists("projectClassName"));
    }
}
