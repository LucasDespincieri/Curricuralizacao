package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    boolean existsByImagemUrl(String imagemUrl);

    // Query Method derivado para filtragem com busca dinâmica por conteúdo da publicação:
    // SQL gerado: SELECT * FROM tb_post WHERE UPPER(conteudo) LIKE UPPER('%' + :termo + '%') ORDER BY data_criacao DESC
    List<Post> findByConteudoContainingIgnoreCaseOrderByDataCriacaoDesc(String termo);

    // Formulário de Filtro do feed: procura o termo no título, no texto ou no nome do autor do post.
    @Query("select p from Post p join p.autor a " +
           "where lower(p.conteudo) like lower(concat('%', :termo, '%')) " +
           "or lower(p.titulo) like lower(concat('%', :termo, '%')) " +
           "or lower(a.nome) like lower(concat('%', :termo, '%')) " +
           "order by p.dataCriacao desc")
    List<Post> buscarNoFeed(@Param("termo") String termo);
}
