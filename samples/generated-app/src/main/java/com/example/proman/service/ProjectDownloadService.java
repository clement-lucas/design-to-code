package com.example.proman.service;

import com.example.proman.entity.ProjectsByUser;
import com.example.proman.entity.ProjectsByUserRequest;
import com.example.proman.repository.ProjectsByUserRepository;
import com.example.proman.repository.ProjectsByUserRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ProjectDownloadService {

    private final ProjectsByUserRepository projectsByUserRepository;
    private final ProjectsByUserRequestRepository requestRepository;

    public ProjectDownloadService(ProjectsByUserRepository projectsByUserRepository,
                                  ProjectsByUserRequestRepository requestRepository) {
        this.projectsByUserRepository = projectsByUserRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ProjectsByUser> findByUserId(Integer userId) {
        return projectsByUserRepository.findById(userId);
    }

    public ProjectsByUserRequest createRequest(Integer userId) {
        ProjectsByUserRequest request = new ProjectsByUserRequest();
        request.setUserId(userId);
        request.setStatus("01"); // 未処理
        request.setRequestDatetime(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public Optional<ProjectsByUserRequest> findRequestById(Long requestId) {
        return requestRepository.findById(requestId);
    }
}
