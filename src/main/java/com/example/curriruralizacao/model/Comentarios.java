package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Marca a classe como uma entidade persistente mapeada para a tabela 'Comentarios' no banco de dados.
@Entity
// Lombok: Gera automaticamente métodos getters e setters de todos os atributos para encapsulamento.
@Getter @Setter
// Lombok: Gera o construtor vazio sem parâmetros obrigatório para o JPA.
@NoArgsConstructor
public class Comentarios {

    // Define o atributo 'id' como chave primária da tabela.
    @Id
    // Configura a geração do identificador para auto-incremento automático gerenciado pelo banco de dados.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador numérico único do comentário.

    // Bean Validation: Garante que o texto do comentário não pode ser nulo.
    @NotNull(message = "o comentário deve conter algum conteúdo")
    private String conteudo; // Conteúdo textual da mensagem/comentário publicado.

    // Mapeia o atributo 'dataCriacao' para a coluna física com nome personalizado "data_2".
    @Column(name = "data_2")
    // Inicializa o campo com a data e horário exatos em que a instância do comentário for criada.
    private LocalDateTime dataCriacao = LocalDateTime.now(); // Timestamp de criação do comentário.

    // Relacionamento Muitos-para-Um: muitos comentários podem ser escritos por um mesmo Usuário (autor).
    @ManyToOne(fetch = FetchType.LAZY)
    // Especifica a coluna de chave estrangeira que armazena a referência ao usuário autor.
    @JoinColumn
    private Usuario autor; // Autor do comentário.

    // Relacionamento Muitos-para-Um: muitos comentários pertencem a uma mesma publicação (Post).
    @ManyToOne(fetch = FetchType.LAZY)
    // Especifica a coluna de chave estrangeira que armazena a referência ao post comentado.
    @JoinColumn
    private Post post; // Post ao qual este comentário pertence.

    // Construtor completo com argumentos para criar uma instância de comentário com seu conteúdo, post e autor.
    public Comentarios(String conteudo, Post post, Usuario autor) {
        this.conteudo = conteudo;
        this.post = post;
        this.autor = autor;
    }

}
