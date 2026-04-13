package com.example.proman.repository;

import com.example.proman.entity.ProjectsByUserRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectsByUserRequestRepository extends JpaRepository<ProjectsByUserRequest, Long> {
}
