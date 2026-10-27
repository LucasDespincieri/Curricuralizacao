package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository: Marca a interface como componente de repositório Spring Data JPA.
// JpaRepository<Livro, Long>: Fornece métodos CRUD completos para a entidade Livro (ID do tipo Long):
// - save(livro), findById(id), findAll(), deleteById(id), etc.
@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {
}
