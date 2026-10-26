package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "tb_capitulo",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_livro_capitulo_ordem",
                columnNames = {"livro_id", "ordem"}
        )
)
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Capitulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer ordem;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String conteudo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    public Capitulo(Integer ordem, String titulo, String conteudo, Livro livro) {
        this.ordem = ordem;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.livro = livro;
    }
}

