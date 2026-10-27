package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// @Entity: Marca esta classe como uma entidade persistente mapeada pelo Hibernate.
@Entity
// @Table: Define o nome da tabela física ("tb_capitulo") e aplica uma restrição de unicidade (unique constraint):
// O par (livro_id, ordem) é único, impedindo que um mesmo livro tenha dois capítulos com o mesmo número de ordem.
@Table(
        name = "tb_capitulo",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_livro_capitulo_ordem",
                columnNames = {"livro_id", "ordem"}
        )
)
// Lombok: Gera automaticamente métodos getters e setters para acesso e modificação dos atributos.
@Getter @Setter
// Lombok: Gera o construtor vazio sem parâmetros obrigatório para o JPA.
@NoArgsConstructor
// Lombok: Gera equals() e hashCode() customizados.
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Capitulo {

    // Define a chave primária da entidade Capítulo.
    @Id
    // Configura geração automática do ID via auto-incremento pelo banco de dados.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador numérico único do capítulo.

    // Número ordinal da sequência do capítulo (ex: Capítulo 1, 2, 3) - campo obrigatório.
    @Column(nullable = false)
    private Integer ordem; // Posição/ordem sequencial do capítulo no livro.

    // Título do capítulo: obrigatório e limitado a até 200 caracteres.
    @Column(nullable = false, length = 200)
    private String titulo; // Título ou nome do capítulo.

    // @Lob: Indica Large Object (objeto grande de dados).
    // columnDefinition = "LONGTEXT": Instrução específica para MySQL armazenar textos extensos (até 4GB).
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String conteudo; // Texto integral completo do capítulo.

    // Relacionamento Muitos-para-Um: muitos capítulos pertencem a um único Livro.
    @ManyToOne(fetch = FetchType.LAZY)
    // Chave estrangeira no banco apontando para o livro proprietário deste capítulo.
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro; // Livro ao qual este capítulo pertence.

    // Relacionamento Um-para-Muitos: um capítulo pode ter vários grifos/marcações de texto feitas por usuários.
    // - cascade = CascadeType.ALL e orphanRemoval = true: se o capítulo for excluído, seus grifos também são.
    @OneToMany(mappedBy = "capitulo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Grifo> grifos = new ArrayList<>(); // Lista de destaques/grifos feitos pelos leitores neste capítulo.

    // Construtor completo para facilitar a criação de capítulos com todos os dados essenciais.
    public Capitulo(Integer ordem, String titulo, String conteudo, Livro livro) {
        this.ordem = ordem;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.livro = livro;
    }
}
