package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Grifo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository: Interface de repositório Spring Data JPA para a entidade Grifo.
// JpaRepository<Grifo, Long>: Gerencia a persistência de marcações/grifos em capítulos (ID do tipo Long).
// Fornece automaticamente operações de CRUD prontas: save(), findById(), deleteById(), etc.
@Repository
public interface GrifoRepository extends JpaRepository<Grifo, Long> {
}
