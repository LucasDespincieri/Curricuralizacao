package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_livro")
@Getter @Setter
@NoArgsConstructor
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(name = "data_publicacao", nullable = false, updatable = false)
    private LocalDateTime dataPublicacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    public Livro(String titulo, Usuario autor) {
        this.titulo = titulo;
        this.autor = autor;
    }

    @PrePersist
    protected void onCreate() {
        if (this.dataPublicacao == null) {
            this.dataPublicacao = LocalDateTime.now();
        }
    }
}
