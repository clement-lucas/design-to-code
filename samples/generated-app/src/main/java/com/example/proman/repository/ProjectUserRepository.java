package com.example.proman.repository;

import com.example.proman.entity.ProjectUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectUserRepository extends JpaRepository<ProjectUser, ProjectUser.ProjectUserId> {
    List<ProjectUser> findByProjectId(Integer projectId);
    void deleteByProjectId(Integer projectId);
}
