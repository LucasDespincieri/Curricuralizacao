package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// @Entity: Mapeia esta classe como uma entidade gerenciada pelo JPA.
@Entity
// @Table: Define o nome da tabela no banco ("tb_reacao_post") e estabelece uma constraint de unicidade:
// A chave única composta (post_id + usuario_id) impede que um mesmo usuário reaja mais de uma vez ao mesmo post.
@Table(
        name = "tb_reacao_post",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_usuario_reacao",
                columnNames = {"post_id", "usuario_id"}
        )
)
// Lombok: Gera automaticamente getters e setters de todos os atributos.
@Getter @Setter
// Lombok: Implementa equals() e hashCode() de maneira personalizada.
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// Lombok: Construtor sem argumentos exigido pelo JPA.
@NoArgsConstructor
public class ReacaoPost {

    // Define a chave primária da reação.
    @Id
    // Configura o valor do identificador com estratégia auto-incremento pelo banco de dados.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // Identificador numérico único da reação.

    // Coluna para armazenar o tipo da reação (ex: "CURTIR", "AMEM", "ORANDO").
    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo; // Tipo textual da reação registrada.

    // Data e horário em que a reação foi efetuada (não nula e imutável após criação).
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao; // Timestamp do momento da reação.

    // Relacionamento Muitos-para-Um: muitas reações apontam para uma única postagem (Post).
    @ManyToOne(fetch = FetchType.LAZY)
    // Chave estrangeira que vincula a reação ao respectivo Post.
    @JoinColumn(name = "post_id")
    private Post post; // Publicação que recebeu a reação.

    // Relacionamento Muitos-para-Um: muitas reações podem ser feitas por um mesmo Usuário.
    @ManyToOne(fetch = FetchType.LAZY)
    // Chave estrangeira que vincula a reação ao usuário que reagiu.
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Usuário autor da reação.

    // Construtor com argumentos para facilitar a instanciação de uma reação.
    public ReacaoPost(String tipo, Post post, Usuario usuario) {
        this.tipo = tipo;
        this.post = post;
        this.usuario = usuario;
        this.dataCriacao = LocalDateTime.now(); // Inicializa com o horário atual ao instanciar
    }

    // Callback de ciclo de vida do JPA garantindo a definição de dataCriacao antes de persistir no banco.
    @PrePersist
    protected void onCreate() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
    }

}