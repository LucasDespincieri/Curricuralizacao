package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// @Repository indica que esta interface pertence à camada de persistência de dados (acesso ao banco).
// JpaRepository<Post, Long>: Gerencia a entidade 'Post', cuja chave primária é do tipo 'Long'.
// Fornece métodos prontos para operações CRUD: save(), findById(), findAll(), deleteById(), etc.
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Query Method derivado: busca todas as postagens publicadas para exibição no feed ordenadas da mais recente para a mais antiga.
    // SQL gerado: SELECT * FROM tb_post ORDER BY data_criacao DESC
    List<Post> findAllByOrderByDataCriacaoDesc();
}
