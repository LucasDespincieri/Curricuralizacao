package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Comentarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository: Interface de repositório Spring Data JPA para a entidade Comentarios.
// JpaRepository<Comentarios, Long>: Gerencia a persistência de comentários (ID do tipo Long).
// Fornece automaticamente operações de CRUD prontas: save(), findById(), deleteById(), etc.
@Repository
public interface ComentraioRepository extends JpaRepository<Comentarios, Long> {
}
