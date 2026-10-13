package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private Integer id;

    @Column
    @Length(max = 50, message = "O nome deve ter no máximo 50 carateres")
    private String nome;

    @Column(unique = true)
    @NotNull(message = "O uso do email é obrigatório")
    private String email;

    private String bairro;

    @Column
    private LocalDate dataNascimento;

    @Column(name = "dia_cadastro")
    private LocalDateTime dataCadastro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCidade")
    private Cidade cidade;

    public Usuario(String nome, String email, LocalDateTime dataCadastro) {
        this.nome = nome;
        this.email = email;
        this.dataCadastro = dataCadastro != null ? dataCadastro : LocalDateTime.now();
    }
}
