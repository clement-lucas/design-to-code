package com.example.proman.repository;

import com.example.proman.entity.BusinessDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessDateRepository extends JpaRepository<BusinessDate, String> {
}
