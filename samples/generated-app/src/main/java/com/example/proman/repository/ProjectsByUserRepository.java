package com.example.proman.repository;

import com.example.proman.entity.ProjectsByUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectsByUserRepository extends JpaRepository<ProjectsByUser, Integer> {
}
