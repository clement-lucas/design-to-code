package com.example.proman.repository;

import com.example.proman.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {

    @Query("SELECT p FROM Project p WHERE " +
           "(:projectName IS NULL OR p.projectName LIKE %:projectName%) AND " +
           "(:projectType IS NULL OR p.projectType = :projectType) AND " +
           "(:projectClass IS NULL OR p.projectClass = :projectClass) AND " +
           "(:organizationId IS NULL OR p.organizationId = :organizationId) AND " +
           "(:clientId IS NULL OR p.clientId = :clientId) AND " +
           "(:startDateFrom IS NULL OR p.projectStartDate >= :startDateFrom) AND " +
           "(:startDateTo IS NULL OR p.projectStartDate <= :startDateTo) AND " +
           "(:endDateFrom IS NULL OR p.projectEndDate >= :endDateFrom) AND " +
           "(:endDateTo IS NULL OR p.projectEndDate <= :endDateTo)")
    Page<Project> searchProjects(
            @Param("projectName") String projectName,
            @Param("projectType") String projectType,
            @Param("projectClass") String projectClass,
            @Param("organizationId") Integer organizationId,
            @Param("clientId") Integer clientId,
            @Param("startDateFrom") LocalDate startDateFrom,
            @Param("startDateTo") LocalDate startDateTo,
            @Param("endDateFrom") LocalDate endDateFrom,
            @Param("endDateTo") LocalDate endDateTo,
            Pageable pageable);

    @Query("SELECT p FROM Project p JOIN ProjectUser pu ON p.projectId = pu.projectId WHERE pu.userId = :userId " +
           "ORDER BY p.projectStartDate ASC, p.projectEndDate ASC, p.projectName ASC")
    List<Project> findProjectsByUserId(@Param("userId") Integer userId);

    @Query("SELECT p FROM Project p WHERE p.projectStartDate >= :startDate AND p.projectEndDate <= :endDate " +
           "ORDER BY p.projectStartDate ASC, p.projectEndDate ASC, p.projectName ASC")
    List<Project> findProjectsByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
