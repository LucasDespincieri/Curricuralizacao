package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class UF {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ufId")
    private Integer id;

    @NotNull
    @Size(max = 2, message = "O UF deve ter no máximo 2 carateres")
    private String UF;

    @ManyToOne
    @JoinColumn(name = "idPais")
    private Pais pais;
}
