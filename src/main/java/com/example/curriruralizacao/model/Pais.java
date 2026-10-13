package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Pais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPais")
    private Long id;

    @NotNull
    @Size(min = 1, max = 50, message = "a descrição deve ter entre 1 a 50 carateres")
    private String descricao;
}
