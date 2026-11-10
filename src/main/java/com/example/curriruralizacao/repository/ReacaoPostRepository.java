package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.ReacaoPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// @Repository: Interface de repositório Spring Data JPA para a entidade ReacaoPost.
// JpaRepository<ReacaoPost, Long>: Fornece métodos de persistência e consulta no banco para ReacaoPost (ID Long).
@Repository
public interface ReacaoPostRepository extends JpaRepository<ReacaoPost, Long> {

    // Query Method derivado: busca a reação de um usuário específico em um post específico.
    // SQL gerado: SELECT * FROM tb_reacao_post WHERE post_id = :postId AND usuario_id = :usuarioId
    Optional<ReacaoPost> findByPostIdAndUsuarioId(Long postId, Integer usuarioId);

    // Query Method derivado: calcula o total absoluto de reações recebidas por uma postagem.
    // SQL gerado: SELECT COUNT(*) FROM tb_reacao_post WHERE post_id = :postId
    long countByPostId(Long postId);

    // Query Method derivado: calcula o número de reações de um tipo específico em um post (ex: quantidade de "CURTIR" ou "AMEM").
    // SQL gerado: SELECT COUNT(*) FROM tb_reacao_post WHERE post_id = :postId AND tipo = :tipo
    long countByPostIdAndTipo(Long postId, String tipo);

    // Query Method derivado: verifica de forma rápida se o usuário já reagiu ao post indicado.
    // SQL gerado: SELECT COUNT(*) > 0 FROM tb_reacao_post WHERE post_id = :postId AND usuario_id = :usuarioId
    boolean existsByPostIdAndUsuarioId(Long postId, Integer usuarioId);
}
