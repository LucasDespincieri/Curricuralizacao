package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Cidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(min = 1, max = 50, message = "A descricao deve ter entre 1 a 50 caracteres")
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "ufId")
    private UF UF;
}
