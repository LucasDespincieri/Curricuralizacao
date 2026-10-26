package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_grifo")
@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Grifo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "texto_selecionado", nullable = false, columnDefinition = "TEXT")
    private String textoSelecionado;

    @Column(columnDefinition = "TEXT")
    private String anotacao;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capitulo_id", nullable = false)
    private Capitulo capitulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Grifo() {
    }

    public Grifo(String textoSelecionado, String anotacao, Capitulo capitulo, Usuario usuario) {
        this.textoSelecionado = textoSelecionado;
        this.anotacao = anotacao;
        this.capitulo = capitulo;
        this.usuario = usuario;
    }

    @PrePersist
    protected void onCreate() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
    }

}
