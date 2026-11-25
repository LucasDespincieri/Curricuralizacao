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

    // Query Method derivado para filtragem com busca dinâmica por título (Formulário de Filtro do CRUD):
    // SQL gerado: SELECT * FROM tb_livro WHERE UPPER(titulo) LIKE UPPER('%' + :titulo + '%') ORDER BY data_publicacao DESC
    List<Livro> findByTituloContainingIgnoreCaseOrderByDataPublicacaoDesc(String titulo);

    // Remove o livro da estante de todos os usuários (tabela de junção do @ManyToMany livrosFavoritos).
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(
            value = "DELETE FROM tb_biblioteca_usuario WHERE livro_id = :livroId", nativeQuery = true)
    void removerDeTodasAsEstantes(@org.springframework.data.repository.query.Param("livroId") Long livroId);

    // Formulário de Filtro da Biblioteca: busca no banco pelo título do livro OU pelo nome do autor.
    @org.springframework.data.jpa.repository.Query(
            "select l from Livro l join l.autor a " +
            "where lower(l.titulo) like lower(concat('%', :termo, '%')) " +
            "   or lower(a.nome) like lower(concat('%', :termo, '%')) " +
            "order by l.dataPublicacao desc")
    List<Livro> buscarPorTituloOuAutor(@org.springframework.data.repository.query.Param("termo") String termo);

    // Livros escritos por um usuário (usado ao excluir a conta do membro).
    List<Livro> findAllByAutor_Id(Integer autorId);
}
