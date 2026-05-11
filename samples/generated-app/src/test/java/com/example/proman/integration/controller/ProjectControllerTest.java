package com.example.proman.integration.controller;

import com.example.proman.security.LoginUserDetails;
import com.example.proman.test.TestSecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
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
@DisplayName("ProjectController 結合テスト")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final LoginUserDetails pmUser = TestSecurityUtils.createPmUser();

    // ==================== プロジェクト登録 ====================

    @Test
    @DisplayName("C03: プロジェクト登録画面 - PM認証済みでアクセス可能")
    void createInit_pmUser_returns200() throws Exception {
        mockMvc.perform(get("/project/create")
                        .with(user(pmUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/create"))
                .andExpect(model().attributeExists("projectTypes"))
                .andExpect(model().attributeExists("organizations"));
    }

    @Test
    @DisplayName("C04: プロジェクト登録画面 - 一般ユーザはアクセス拒否")
    @WithMockUser(username = "member", roles = {"USER"})
    void createInit_normalUser_returns403() throws Exception {
        mockMvc.perform(get("/project/create"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("C05: プロジェクト登録画面 - 未認証でリダイレクト")
    void createInit_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/project/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("C06: プロジェクト登録確認 - 有効なフォームで確認画面表示")
    void createConfirm_validForm_returnsConfirmPage() throws Exception {
        mockMvc.perform(post("/project/create/confirm")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectName", "テストプロジェクト")
                        .param("projectType", "01")
                        .param("projectClass", "A")
                        .param("projectStartDate", "2024/01/01")
                        .param("projectEndDate", "2024/12/31")
                        .param("organizationId", "1")
                        .param("clientId", "1")
                        .param("projectManager", "山田太郎")
                        .param("projectLeader", "田中花子"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/confirmCreate"));
    }

    @Test
    @DisplayName("C07: プロジェクト登録確認 - 必須項目未入力でエラー")
    void createConfirm_emptyForm_returnsCreateWithErrors() throws Exception {
        mockMvc.perform(post("/project/create/confirm")
                        .with(user(pmUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("project/create"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("C08: プロジェクト登録確認 - 終了日<開始日でバリデーションエラー")
    void createConfirm_endDateBeforeStartDate_returnsError() throws Exception {
        mockMvc.perform(post("/project/create/confirm")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectName", "テストプロジェクト")
                        .param("projectType", "01")
                        .param("projectClass", "A")
                        .param("projectStartDate", "2024/12/31")
                        .param("projectEndDate", "2024/01/01")
                        .param("organizationId", "1")
                        .param("clientId", "1")
                        .param("projectManager", "山田太郎")
                        .param("projectLeader", "田中花子"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/create"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("C09: プロジェクト登録実行 - 正常な登録で完了画面にリダイレクト")
    void createExecute_validForm_redirectsToComplete() throws Exception {
        mockMvc.perform(post("/project/create/execute")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectName", "新規プロジェクト")
                        .param("projectType", "01")
                        .param("projectClass", "A")
                        .param("projectStartDate", "2024/01/01")
                        .param("projectEndDate", "2024/12/31")
                        .param("organizationId", "2")
                        .param("clientId", "1")
                        .param("projectManager", "山田太郎")
                        .param("projectLeader", "田中花子"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/project/create/complete"));
    }

    @Test
    @DisplayName("C10: プロジェクト登録実行 - 戻るボタンで登録画面に戻る")
    void createExecute_backButton_returnsCreatePage() throws Exception {
        mockMvc.perform(post("/project/create/execute")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectName", "テストプロジェクト")
                        .param("projectType", "01")
                        .param("projectClass", "A")
                        .param("projectStartDate", "2024/01/01")
                        .param("projectEndDate", "2024/12/31")
                        .param("organizationId", "1")
                        .param("clientId", "1")
                        .param("projectManager", "山田太郎")
                        .param("projectLeader", "田中花子")
                        .param("back", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("project/create"));
    }

    @Test
    @DisplayName("C11: プロジェクト登録完了画面表示")
    void createComplete_returns200() throws Exception {
        mockMvc.perform(get("/project/create/complete")
                        .with(user(pmUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/completeCreate"));
    }

    // ==================== プロジェクト検索 ====================

    @Test
    @DisplayName("C12: プロジェクト検索画面 - 初期表示")
    void searchInit_authenticated_returnsSearchPage() throws Exception {
        mockMvc.perform(get("/project/search")
                        .with(user(pmUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/search"))
                .andExpect(model().attributeExists("projectTypes"))
                .andExpect(model().attributeExists("projectClasses"));
    }

    @Test
    @DisplayName("C13: プロジェクト検索実行 - 条件なし全件検索")
    void searchExecute_noConditions_returnsResults() throws Exception {
        mockMvc.perform(get("/project/search/search")
                        .with(user(pmUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/search"))
                .andExpect(model().attributeExists("searchExecuted"));
    }

    @Test
    @DisplayName("C14: プロジェクト検索 - 一致しない条件でエラーメッセージ")
    void searchExecute_noMatch_showsErrorMessage() throws Exception {
        mockMvc.perform(get("/project/search/search")
                        .with(user(pmUser))
                        .param("projectName", "存在しないプロジェクト名XYZ"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/search"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // ==================== プロジェクト詳細 ====================

    @Test
    @DisplayName("C15: プロジェクト詳細 - 存在するID")
    void detail_existingId_returnsDetailPage() throws Exception {
        mockMvc.perform(get("/project/detail")
                        .with(user(pmUser))
                        .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/detail"))
                .andExpect(model().attributeExists("project"));
    }

    // ==================== プロジェクト更新 ====================

    @Test
    @DisplayName("C17: プロジェクト更新画面 - PM認証済みで表示")
    void updateInit_pmUser_returnsUpdatePage() throws Exception {
        mockMvc.perform(get("/project/update")
                        .with(user(pmUser))
                        .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/update"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    @DisplayName("C18: プロジェクト更新確認 - 有効なフォーム")
    void updateConfirm_validForm_returnsConfirmPage() throws Exception {
        mockMvc.perform(post("/project/update/confirm")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectId", "1")
                        .param("projectName", "更新テストプロジェクト")
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
    }

    @Test
    @DisplayName("C19: プロジェクト更新確認 - バリデーションエラー")
    void updateConfirm_invalidForm_returnsUpdatePage() throws Exception {
        mockMvc.perform(post("/project/update/confirm")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/update"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("C20: プロジェクト更新実行 - 正常な更新で完了画面にリダイレクト")
    void updateExecute_validForm_redirectsToComplete() throws Exception {
        mockMvc.perform(post("/project/update/execute")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectId", "1")
                        .param("projectName", "更新プロジェクト")
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
    }

    @Test
    @DisplayName("C21: プロジェクト更新実行 - 戻るボタンで更新画面に戻る")
    void updateExecute_backButton_returnsUpdatePage() throws Exception {
        mockMvc.perform(post("/project/update/execute")
                        .with(user(pmUser))
                        .with(csrf())
                        .param("projectId", "1")
                        .param("projectName", "テストプロジェクト")
                        .param("projectType", "01")
                        .param("projectClass", "A")
                        .param("projectStartDate", "2024/01/01")
                        .param("projectEndDate", "2024/12/31")
                        .param("organizationId", "1")
                        .param("clientId", "1")
                        .param("projectManager", "山田太郎")
                        .param("projectLeader", "田中花子")
                        .param("versionNo", "1")
                        .param("back", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("project/update"));
    }
}
