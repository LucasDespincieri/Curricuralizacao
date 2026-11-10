package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// @Repository: Marca a interface como componente de repositório Spring Data JPA.
// JpaRepository<Livro, Long>: Fornece métodos CRUD completos para a entidade Livro (ID do tipo Long):
// - save(livro), findById(id), findAll(), deleteById(id), etc.
@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {

    // Query Method derivado: retorna todas as obras cadastradas na biblioteca em ordem cronológica decrescente de publicação.
    // SQL gerado: SELECT * FROM tb_livro ORDER BY data_publicacao DESC
    List<Livro> findAllByOrderByDataPublicacaoDesc();
}
