package com.example.proman.unit.service;

import com.example.proman.entity.Client;
import com.example.proman.entity.Organization;
import com.example.proman.entity.Project;
import com.example.proman.entity.ProjectUser;
import com.example.proman.repository.*;
import com.example.proman.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService 単体テスト")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectUserRepository projectUserRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setProjectId(1);
        testProject.setProjectName("テストプロジェクト");
        testProject.setProjectType("01");
        testProject.setProjectClass("A");
        testProject.setProjectStartDate(LocalDate.of(2024, 1, 1));
        testProject.setProjectEndDate(LocalDate.of(2024, 12, 31));
        testProject.setOrganizationId(1);
        testProject.setClientId(1);
        testProject.setProjectManager("山田太郎");
        testProject.setProjectLeader("田中花子");
        testProject.setVersionNo(1L);
    }

    @Test
    @DisplayName("S01: searchProjects - 全パラメータnullで全件検索")
    void searchProjects_allNull_returnsAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Project> expected = new PageImpl<>(List.of(testProject));
        when(projectRepository.searchProjects(isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(expected);

        Page<Project> result = projectService.searchProjects(
                null, null, null, null, null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(projectRepository).searchProjects(isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable));
    }

    @Test
    @DisplayName("S02: searchProjects - プロジェクト名指定検索")
    void searchProjects_withProjectName_filtersResults() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Project> expected = new PageImpl<>(List.of(testProject));
        when(projectRepository.searchProjects(eq("テスト"), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(expected);

        Page<Project> result = projectService.searchProjects(
                "テスト", null, null, null, null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProjectName()).isEqualTo("テストプロジェクト");
    }

    @Test
    @DisplayName("S03: searchProjects - 空文字はnullに変換")
    void searchProjects_blankString_convertedToNull() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Project> expected = new PageImpl<>(List.of(testProject));
        when(projectRepository.searchProjects(isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(expected);

        Page<Project> result = projectService.searchProjects(
                "", "  ", "", null, null, null, null, null, null, pageable);

        assertThat(result).isNotNull();
        verify(projectRepository).searchProjects(isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable));
    }

    @Test
    @DisplayName("S04: findById - 存在するID")
    void findById_existingId_returnsProject() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(testProject));

        Optional<Project> result = projectService.findById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getProjectName()).isEqualTo("テストプロジェクト");
    }

    @Test
    @DisplayName("S05: findById - 存在しないID")
    void findById_nonExistingId_returnsEmpty() {
        when(projectRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Project> result = projectService.findById(999);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("S06: createProject - versionNo=1がセットされ保存")
    void createProject_setsVersionNoAndSaves() {
        Project newProject = new Project();
        newProject.setProjectName("新規プロジェクト");
        when(projectRepository.save(any(Project.class))).thenReturn(newProject);

        projectService.createProject(newProject);

        assertThat(newProject.getVersionNo()).isEqualTo(1L);
        verify(projectRepository).save(newProject);
    }

    @Test
    @DisplayName("S07: updateProject - 保存が呼ばれる")
    void updateProject_savesProject() {
        when(projectRepository.save(testProject)).thenReturn(testProject);

        Project result = projectService.updateProject(testProject);

        assertThat(result).isEqualTo(testProject);
        verify(projectRepository).save(testProject);
    }

    @Test
    @DisplayName("S08: findProjectUsers - プロジェクト担当者リスト返却")
    void findProjectUsers_returnsList() {
        ProjectUser pu = new ProjectUser();
        when(projectUserRepository.findByProjectId(1)).thenReturn(List.of(pu));

        List<ProjectUser> result = projectService.findProjectUsers(1);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("S09: findAllOrganizations - 全組織リスト返却")
    void findAllOrganizations_returnsList() {
        Organization org = new Organization();
        org.setOrganizationName("本社");
        when(organizationRepository.findAll()).thenReturn(List.of(org));

        List<Organization> result = projectService.findAllOrganizations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrganizationName()).isEqualTo("本社");
    }

    @Test
    @DisplayName("S10: findOrganizationById - 存在するID")
    void findOrganizationById_returnsOrganization() {
        Organization org = new Organization();
        org.setOrganizationName("開発部");
        when(organizationRepository.findById(2)).thenReturn(Optional.of(org));

        Optional<Organization> result = projectService.findOrganizationById(2);

        assertThat(result).isPresent();
        assertThat(result.get().getOrganizationName()).isEqualTo("開発部");
    }

    @Test
    @DisplayName("S11: searchClients - 条件指定検索")
    void searchClients_withConditions_returnsList() {
        Client client = new Client();
        client.setClientName("サンプル顧客A");
        when(clientRepository.searchClients("サンプル", "01")).thenReturn(List.of(client));

        List<Client> result = projectService.searchClients("サンプル", "01");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientName()).isEqualTo("サンプル顧客A");
    }

    @Test
    @DisplayName("S12: searchClients - 空文字パラメータはnull変換")
    void searchClients_blankParams_convertedToNull() {
        when(clientRepository.searchClients(isNull(), isNull())).thenReturn(Collections.emptyList());

        List<Client> result = projectService.searchClients("", "  ");

        assertThat(result).isEmpty();
        verify(clientRepository).searchClients(isNull(), isNull());
    }

    @Test
    @DisplayName("S13: findClientById - 存在するID")
    void findClientById_returnsClient() {
        Client client = new Client();
        client.setClientName("サンプル顧客A");
        when(clientRepository.findById(1)).thenReturn(Optional.of(client));

        Optional<Client> result = projectService.findClientById(1);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("S14: findProjectsByUserId - プロジェクトリスト返却")
    void findProjectsByUserId_returnsList() {
        when(projectRepository.findProjectsByUserId(1)).thenReturn(List.of(testProject));

        List<Project> result = projectService.findProjectsByUserId(1);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("S15: findProjectsByPeriod - 期間指定検索")
    void findProjectsByPeriod_returnsList() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);
        when(projectRepository.findProjectsByPeriod(start, end)).thenReturn(List.of(testProject));

        List<Project> result = projectService.findProjectsByPeriod(start, end);

        assertThat(result).hasSize(1);
    }
}
