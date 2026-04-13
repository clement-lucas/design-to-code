package com.example.proman.service;

import com.example.proman.entity.Client;
import com.example.proman.entity.Organization;
import com.example.proman.entity.Project;
import com.example.proman.entity.ProjectUser;
import com.example.proman.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final OrganizationRepository organizationRepository;
    private final ClientRepository clientRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectUserRepository projectUserRepository,
                          OrganizationRepository organizationRepository,
                          ClientRepository clientRepository) {
        this.projectRepository = projectRepository;
        this.projectUserRepository = projectUserRepository;
        this.organizationRepository = organizationRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public Page<Project> searchProjects(String projectName, String projectType,
                                        String projectClass, Integer organizationId,
                                        Integer clientId,
                                        LocalDate startDateFrom, LocalDate startDateTo,
                                        LocalDate endDateFrom, LocalDate endDateTo,
                                        Pageable pageable) {
        return projectRepository.searchProjects(
                blankToNull(projectName), blankToNull(projectType),
                blankToNull(projectClass), organizationId,
                clientId, startDateFrom, startDateTo,
                endDateFrom, endDateTo, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Project> findById(Integer projectId) {
        return projectRepository.findById(projectId);
    }

    public Project createProject(Project project) {
        project.setVersionNo(1L);
        return projectRepository.save(project);
    }

    public Project updateProject(Project project) {
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectUser> findProjectUsers(Integer projectId) {
        return projectUserRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<Organization> findAllOrganizations() {
        return organizationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Organization> findOrganizationById(Integer id) {
        return organizationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Client> searchClients(String clientName, String industryCode) {
        return clientRepository.searchClients(blankToNull(clientName), blankToNull(industryCode));
    }

    @Transactional(readOnly = true)
    public Optional<Client> findClientById(Integer id) {
        return clientRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Project> findProjectsByUserId(Integer userId) {
        return projectRepository.findProjectsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Project> findProjectsByPeriod(LocalDate startDate, LocalDate endDate) {
        return projectRepository.findProjectsByPeriod(startDate, endDate);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
