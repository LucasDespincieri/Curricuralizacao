package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// @Entity: Informa ao JPA que esta classe é mapeada para uma tabela no banco de dados.
@Entity
// @Table: Define o nome da tabela física no banco como "tb_livro".
@Table(name = "tb_livro")
// Lombok: Gera automaticamente métodos getters e setters para todos os campos.
@Getter @Setter
// Lombok: Cria o construtor vazio obrigatório para o funcionamento do JPA/Hibernate.
@NoArgsConstructor
public class Livro {

    // Define a chave primária da entidade Livro.
    @Id
    // Configura o valor do ID para ser gerado por auto-incremento pelo banco de dados.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador numérico único do livro.

    // Título do livro: obrigatório (não nulo) e com tamanho máximo de 200 caracteres.
    @Column(nullable = false, length = 200)
    private String titulo; // Título da obra/livro.

    // Data de publicação do livro no sistema: obrigatória e não modificável após inserção.
    @Column(name = "data_publicacao", nullable = false, updatable = false)
    private LocalDateTime dataPublicacao; // Data e hora em que a obra foi cadastrada.

    // Relacionamento Muitos-para-Um: muitos livros podem ser escritos por um mesmo autor (Usuário).
    @ManyToOne(fetch = FetchType.LAZY)
    // Chave estrangeira que aponta para a tabela de usuários (autor_id).
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor; // Usuário autor da publicação deste livro.

    // Relacionamento Um-para-Muitos: um livro é composto por múltiplos capítulos.
    // - mappedBy = "livro": aponta para o campo 'livro' na classe Capitulo.
    // - cascade = CascadeType.ALL: todas as operações no livro (salvar, remover) propagam aos seus capítulos.
    // - orphanRemoval = true: remove automaticamente do banco capítulos desvinculados desta lista.
    // - fetch = FetchType.LAZY: carregamento sob demanda dos capítulos.
    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // Mantém a lista sempre ordenada crescentemente pelo número da ordem do capítulo (Capítulo 1, 2, 3...).
    @OrderBy("ordem ASC")
    private List<Capitulo> capitulos = new ArrayList<>(); // Lista de capítulos pertencentes ao livro.

    // Construtor auxiliar para criar uma instância do livro com seu título e autor.
    public Livro(String titulo, Usuario autor) {
        this.titulo = titulo;
        this.autor = autor;
    }

    // Callback executado antes da primeira gravação no banco para definir a data de publicação caso não fornecida.
    @PrePersist
    protected void onCreate() {
        if (this.dataPublicacao == null) {
            this.dataPublicacao = LocalDateTime.now();
        }
    }
}
