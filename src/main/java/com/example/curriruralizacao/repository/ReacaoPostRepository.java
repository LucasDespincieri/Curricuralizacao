package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.ReacaoPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// @Repository: Interface de repositório Spring Data JPA para a entidade ReacaoPost.
// JpaRepository<ReacaoPost, Long>: Fornece métodos de persistência e consulta no banco para ReacaoPost (ID Long).
@Repository
public interface ReacaoPostRepository extends JpaRepository<ReacaoPost, Long> {
    Optional<ReacaoPost> findByPostIdAndUsuarioId(Long postId, Integer usuarioId);

    long countByPostId(Long postId);

    long countByPostIdAndTipo(Long postId, String tipo);

    boolean existsByPostIdAndUsuarioId(Long postId, Long usuarioId);
}
