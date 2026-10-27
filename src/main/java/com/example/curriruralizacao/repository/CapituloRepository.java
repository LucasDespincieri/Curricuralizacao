package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Capitulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository: Indica que esta interface é um componente de acesso ao banco gerenciado pelo Spring Data JPA.
// JpaRepository<Capitulo, Long>: Fornece operações de banco para a entidade Capitulo com ID do tipo Long.
@Repository
public interface CapituloRepository extends JpaRepository<Capitulo, Long> {

    // Query Method derivado: busca todos os capítulos de um determinado livro pelo ID do livro
    // e os ordena de forma ascendente pela coluna 'ordem' (Capítulo 1, 2, 3...).
    // SQL gerado: SELECT * FROM tb_capitulo WHERE livro_id = :livroId ORDER BY ordem ASC
    List<Capitulo> findByLivroIdOrderByOrdemAsc(Long livroId);

    // Query Method derivado: busca um capítulo específico de um livro a partir da sua ordem/número.
    // SQL gerado: SELECT * FROM tb_capitulo WHERE livro_id = :livroId AND ordem = :ordem
    // Retorna Optional para facilitar a verificação se o capítulo existe ou não.
    Optional<Capitulo> findByLivroIdAndOrdem(Long livroId, Integer ordem);

    // Query Method derivado: verifica se já existe um capítulo com o número de ordem especificado para aquele livro.
    // SQL gerado: SELECT COUNT(*) > 0 FROM tb_capitulo WHERE livro_id = :livroId AND ordem = :ordem
    // Útil para validação antes de cadastrar um novo capítulo e evitar violação da constraint de unicidade.
    boolean existsByLivroIdAndOrdem(Long livroId, Integer ordem);
}
