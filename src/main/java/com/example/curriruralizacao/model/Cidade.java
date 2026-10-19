package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Define que esta classe é uma entidade gerenciada pelo JPA (Java Persistence API), mapeada para uma tabela no banco de dados.
@Entity
// Lombok: Gera automaticamente todos os métodos Getters e Setters para os atributos da classe, evitando código repetitivo.
@Getter @Setter
// Lombok: Gera automaticamente um construtor padrão sem parâmetros, obrigatório pela especificação do JPA.
@NoArgsConstructor
public class Cidade {

    // Indica que o atributo 'id' é a chave primária (Primary Key) da tabela.
    @Id
    // Configura a estratégia de geração automática do ID pelo banco de dados (auto-incremento).
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Identificador único da cidade.

    // Validação de dados (Bean Validation): exige que a descrição tenha entre 1 e 50 caracteres.
    @Size(min = 1, max = 50, message = "A descricao deve ter entre 1 a 50 caracteres")
    private String descricao; // Nome ou descrição da cidade (ex: "São Paulo", "Rio de Janeiro").

    // Relacionamento Muitos-para-Um: muitas cidades pertencem a uma única Unidade Federativa (UF).
    @ManyToOne
    // Especifica o nome da coluna de chave estrangeira (Foreign Key) na tabela 'Cidade' que aponta para 'UF'.
    @JoinColumn(name = "ufId")
    private UF UF; // Referência à Unidade Federativa (estado) à qual esta cidade pertence.
}
