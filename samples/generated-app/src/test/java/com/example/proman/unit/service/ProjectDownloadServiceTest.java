package com.example.proman.unit.service;

import com.example.proman.entity.ProjectsByUser;
import com.example.proman.entity.ProjectsByUserRequest;
import com.example.proman.repository.ProjectsByUserRepository;
import com.example.proman.repository.ProjectsByUserRequestRepository;
import com.example.proman.service.ProjectDownloadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectDownloadService 単体テスト")
class ProjectDownloadServiceTest {

    @Mock
    private ProjectsByUserRepository projectsByUserRepository;
    @Mock
    private ProjectsByUserRequestRepository requestRepository;

    @InjectMocks
    private ProjectDownloadService projectDownloadService;

    @Test
    @DisplayName("S20: findByUserId - 存在するユーザID")
    void findByUserId_existing_returnsProjectsByUser() {
        ProjectsByUser pbu = new ProjectsByUser();
        when(projectsByUserRepository.findById(1)).thenReturn(Optional.of(pbu));

        Optional<ProjectsByUser> result = projectDownloadService.findByUserId(1);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("S21: createRequest - ステータス01で保存")
    void createRequest_createsWithStatusUnprocessed() {
        ProjectsByUserRequest saved = new ProjectsByUserRequest();
        saved.setStatus("01");
        when(requestRepository.save(any(ProjectsByUserRequest.class))).thenReturn(saved);

        ProjectsByUserRequest result = projectDownloadService.createRequest(1);

        assertThat(result.getStatus()).isEqualTo("01");
        verify(requestRepository).save(any(ProjectsByUserRequest.class));
    }

    @Test
    @DisplayName("S22: findRequestById - 存在するリクエストID")
    void findRequestById_existing_returnsRequest() {
        ProjectsByUserRequest request = new ProjectsByUserRequest();
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        Optional<ProjectsByUserRequest> result = projectDownloadService.findRequestById(1L);

        assertThat(result).isPresent();
    }
}
