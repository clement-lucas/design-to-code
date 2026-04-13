package com.example.proman.repository;

import com.example.proman.entity.CodeName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CodeNameRepository extends JpaRepository<CodeName, CodeName.CodeNameId> {

    @Query("SELECT c FROM CodeName c WHERE c.codeId = :codeId AND c.lang = :lang ORDER BY c.sortOrder")
    List<CodeName> findByCodeIdAndLang(@Param("codeId") String codeId, @Param("lang") String lang);

    default List<CodeName> findByCodeIdJa(String codeId) {
        return findByCodeIdAndLang(codeId, "ja");
    }
}
