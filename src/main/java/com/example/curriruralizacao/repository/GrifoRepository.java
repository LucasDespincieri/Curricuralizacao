package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Grifo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// @Repository: Interface de repositório Spring Data JPA para a entidade Grifo.
// JpaRepository<Grifo, Long>: Gerencia a persistência de marcações/grifos em capítulos (ID do tipo Long).
// Fornece automaticamente operações de CRUD prontas: save(), findById(), deleteById(), etc.
@Repository
public interface GrifoRepository extends JpaRepository<Grifo, Long> {

    // Query Method derivado: busca todos os grifos de um usuário específico em um determinado capítulo.
    // SQL gerado: SELECT * FROM tb_grifo WHERE capitulo_id = :capituloId AND usuario_id = :usuarioId
    List<Grifo> findAllByCapituloIdAndUsuarioId(Long capituloId, Integer usuarioId);

    // Query Method derivado: lista todas as anotações e reflexões feitas pelo membro em qualquer livro, ordenadas da mais recente para a mais antiga.
    // SQL gerado: SELECT * FROM tb_grifo WHERE usuario_id = :usuarioId ORDER BY data_criacao DESC
    List<Grifo> findAllByUsuarioIdOrderByDataCriacaoDesc(Integer usuarioId);
}
