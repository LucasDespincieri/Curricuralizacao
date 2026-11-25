package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Pais;
import com.example.curriruralizacao.model.UF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UFRepository extends JpaRepository<UF, Integer> {

    // JPQL explícito porque o atributo se chama "UF" (maiúsculo) e a derivação por nome de método se confunde.
    @Query("select u from UF u where upper(u.UF) = upper(:sigla) and u.pais = :pais")
    Optional<UF> buscarPorSigla(@Param("sigla") String sigla, @Param("pais") Pais pais);
}
