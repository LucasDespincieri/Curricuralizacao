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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Usuario autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Post post;

    public Comentarios(String conteudo, Post post, Usuario autor) {
        this.conteudo = conteudo;
        this.post = post;
        this.autor = autor;
    }

}
