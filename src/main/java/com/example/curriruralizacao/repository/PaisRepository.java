package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaisRepository extends JpaRepository<Pais, Long> {

    @Query("select p from Pais p where lower(p.descricao) = lower(:descricao)")
    Optional<Pais> buscarPorDescricao(@Param("descricao") String descricao);
}
