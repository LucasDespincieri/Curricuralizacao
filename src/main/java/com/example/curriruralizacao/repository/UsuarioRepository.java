package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
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
    // @EntityGraph: já traz a cidade (e a UF) junto com o usuário. Sem isso, ler a cidade depois que a consulta
    // terminou (ex.: ao montar a resposta do login ou do /me) dava LazyInitializationException.
    @EntityGraph(attributePaths = {"cidade"})
    Optional<Usuario> findByEmail(String email); // Usado principalmente no fluxo de login/autenticação.

    // Query Method derivado para checagem rápida de existência:
    // SELECT COUNT(*) > 0 FROM usuario WHERE email = :email
    // Retorna boolean direto sem carregar todo o objeto na memória (ideal para validação de unicidade de e-mail no cadastro).
    boolean existsByEmail(String email); // Verifica se já existe um usuário cadastrado com o e-mail informado.

    // Quantos membros moram na cidade (impede excluir cidade em uso).
    long countByCidade_Id(Integer cidadeId);

    // [id da cidade, quantidade de membros] para todas as cidades que têm algum morador.
    @org.springframework.data.jpa.repository.Query(
            "select u.cidade.id, count(u) from Usuario u where u.cidade is not null group by u.cidade.id")
    List<Object[]> contarMembrosPorCidade();

    // Formulário de Filtro do CRUD de Membros: busca por nome ou e-mail.
    @org.springframework.data.jpa.repository.Query(
            "select u from Usuario u " +
            "where lower(u.nome) like lower(concat('%', :termo, '%')) " +
            "   or lower(u.email) like lower(concat('%', :termo, '%')) " +
            "order by u.nome")
    List<Usuario> buscarPorNomeOuEmail(@org.springframework.data.repository.query.Param("termo") String termo);

    List<Usuario> findAllByOrderByNomeAsc();

    long countByRole(com.example.curriruralizacao.model.ENUMS.Roles role);
}
