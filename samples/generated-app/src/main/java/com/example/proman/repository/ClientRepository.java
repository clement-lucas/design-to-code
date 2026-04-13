package com.example.proman.repository;

import com.example.proman.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Integer> {

    @Query("SELECT c FROM Client c WHERE " +
           "(:clientName IS NULL OR c.clientName LIKE %:clientName%) AND " +
           "(:industryCode IS NULL OR c.industryCode = :industryCode)")
    List<Client> searchClients(@Param("clientName") String clientName,
                               @Param("industryCode") String industryCode);
}
