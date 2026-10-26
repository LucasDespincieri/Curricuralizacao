package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Livro, Long> {
}
