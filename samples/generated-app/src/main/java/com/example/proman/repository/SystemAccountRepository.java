package com.example.proman.repository;

import com.example.proman.entity.SystemAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SystemAccountRepository extends JpaRepository<SystemAccount, Integer> {
    Optional<SystemAccount> findByLoginId(String loginId);
}
