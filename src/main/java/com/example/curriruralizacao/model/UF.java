package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Mapeia a classe como uma entidade persistente da tabela de UFs (estados) no banco de dados.
@Entity
// Lombok: Gera automaticamente métodos getters e setters de todos os campos.
@Getter @Setter
// Lombok: Gera construtor sem argumentos exigido pelo JPA.
@NoArgsConstructor
public class UF {

    // Define a chave primária da entidade UF.
    @Id
    // Configura a estratégia de incremento automático gerada pelo próprio banco relacional.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Especifica o nome da coluna no banco como "ufId".
    @Column(name = "ufId")
    private Integer id; // Identificador numérico único da Unidade Federativa.

    // Bean Validation: Garante que a sigla do estado é de preenchimento obrigatório (não nula).
    @NotNull
    // Bean Validation: Define que a sigla deve ter no máximo 2 caracteres (ex: "SP", "RJ", "MG").
    @Size(max = 2, message = "O UF deve ter no máximo 2 carateres")
    private String UF; // Sigla da Unidade Federativa.

    // Relacionamento Muitos-para-Um: muitas UFs pertencem a um único País.
    @ManyToOne
    // Especifica a coluna no banco que armazena a chave estrangeira (Foreign Key) referenciando Pais.
    @JoinColumn(name = "idPais")
    private Pais pais; // Objeto de entidade Pais ao qual este estado pertence.
}
