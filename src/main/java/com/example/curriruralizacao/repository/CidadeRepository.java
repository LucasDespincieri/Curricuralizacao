package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Cidade;
import com.example.curriruralizacao.model.UF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository: Interface de repositório Spring Data JPA para a entidade Cidade (CRUD de Tabela Cadastral).
@Repository
public interface CidadeRepository extends JpaRepository<Cidade, Integer> {

    // Retorna todas as cidades cadastradas em ordem alfabética.
    List<Cidade> findAllByOrderByDescricaoAsc();

    // Query Method derivado para filtragem com busca dinâmica insensível a maiúsculas/minúsculas (Formulário de Filtro do CRUD):
    // SQL equivalente gerado: SELECT * FROM Cidade WHERE UPPER(descricao) LIKE UPPER('%' + :termo + '%') ORDER BY descricao ASC
    List<Cidade> findByDescricaoContainingIgnoreCaseOrderByDescricaoAsc(String termo);

    // Busca uma cidade pelo nome (sem diferenciar maiúsculas/minúsculas) dentro de uma UF específica.
    @Query("select c from Cidade c where lower(c.descricao) = lower(:nome) and c.UF = :uf")
    Optional<Cidade> buscarPorNomeEUf(@Param("nome") String nome, @Param("uf") UF uf);
}
