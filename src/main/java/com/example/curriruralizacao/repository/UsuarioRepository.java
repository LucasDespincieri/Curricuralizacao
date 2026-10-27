package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository: Anotação que indica ao Spring que esta interface pertence à camada de persistência (acesso a dados).
// JpaRepository<Usuario, Integer>: Interface do Spring Data JPA que fornece métodos CRUD completos pré-implementados:
// - save(), findById(), findAll(), deleteById(), count(), etc.
// - Parâmetros genéricos: Tipo da entidade (Usuario) e Tipo da chave primária (Integer).
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Query Method derivado: O Spring Data JPA analisa o nome do método ("findByNome")
    // e constrói dinamicamente em tempo de execução a query SQL equivalente:
    // SELECT * FROM usuario WHERE nome = :nome
    List<Usuario> findByNome(String nome); // Retorna uma lista de usuários que possuem o nome informado.

    // Query Method derivado para busca por e-mail:
    // SELECT * FROM usuario WHERE email = :email
    // Retorna Optional<Usuario> para tratar com segurança cenários onde o e-mail não for encontrado (evitando NullPointerException).
    Optional<Usuario> findByEmail(String email); // Usado principalmente no fluxo de login/autenticação.

    // Query Method derivado para checagem rápida de existência:
    // SELECT COUNT(*) > 0 FROM usuario WHERE email = :email
    // Retorna boolean direto sem carregar todo o objeto na memória (ideal para validação de unicidade de e-mail no cadastro).
    boolean existsByEmail(String email); // Verifica se já existe um usuário cadastrado com o e-mail informado.
}
