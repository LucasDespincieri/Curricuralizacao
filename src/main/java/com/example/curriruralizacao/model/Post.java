package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// @Entity: Mapeia esta classe como uma entidade persistente no banco de dados gerenciada pelo JPA/Hibernate.
@Entity
// Lombok: Gera automaticamente todos os métodos Getters e Setters dos atributos da classe.
@Getter @Setter
// @Table: Define explicitamente o nome da tabela física no banco de dados como "tb_post".
@Table(name = "tb_post")
// Lombok: Gera o construtor público sem argumentos, exigido pela especificação JPA.
@NoArgsConstructor
// Lombok: Implementa equals() e hashCode() considerando apenas atributos anotados com @EqualsAndHashCode.Include.
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Post {

    // Define 'id' como a chave primária da entidade.
    @Id
    // Configura a geração do valor do ID por auto-incremento gerenciado pelo banco de dados.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Especifica o nome da coluna física no banco de dados como "id".
    @Column(name = "id")
    private Long id; // Identificador único da publicação.

    // Coluna para armazenar o título da postagem.
    @Column(name = "titulo")
    private String titulo; // Título descritivo do post.

    // Define a coluna de texto como obrigatória (nullable = false) e do tipo TEXT (suporta textos longos).
    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo; // Corpo textual completo da postagem.

    // Registra o instante de criação: não pode ser nulo e não deve ser modificado após o insert (updatable = false).
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao; // Data e hora em que a publicação foi realizada.

    // Relacionamento Um-para-Muitos: uma postagem pode ter múltiplos comentários.
    // - mappedBy = "post": aponta para o atributo 'post' dentro da classe Comentarios que é dona do relacionamento.
    // - cascade = CascadeType.ALL: propaga operações (inserir, atualizar, deletar) para os comentários vinculados.
    // - orphanRemoval = true: remove comentários do banco caso sejam excluídos desta lista.
    // - fetch = FetchType.LAZY: carrega a lista sob demanda (apenas quando acessada).
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // Ordena os comentários do mais antigo para o mais recente pela data de criação.
    @OrderBy("dataCriacao ASC")
    private List<Comentarios> comentarios = new ArrayList<>(); // Lista de comentários feitos nesta publicação.

    // Relacionamento Muitos-para-Um: muitas publicações são escritas por um mesmo Usuário (autor).
    @ManyToOne(fetch = FetchType.LAZY)
    // Chave estrangeira (Foreign Key) na tabela 'tb_post' que referencia o autor na tabela de usuários.
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor; // Usuário que criou/publicou o post.

    // Relacionamento Um-para-Muitos: uma publicação pode receber múltiplas reações (curtidas, apoios, etc.).
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReacaoPost> reacoes = new ArrayList<>(); // Lista de reações que este post recebeu.

    // Construtor auxiliar para instanciar posts informando o conteúdo e o autor diretamente.
    public Post(String conteudo, Usuario autor) {
        this.conteudo = conteudo;
        this.autor = autor;
    }

    // Callback de ciclo de vida do JPA: executado automaticamente antes de persistir o post pela primeira vez no banco.
    @PrePersist
    protected void onCreate() {
        // Se a data de criação não foi previamente definida, atribui o timestamp do momento exato atual.
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
    }

}