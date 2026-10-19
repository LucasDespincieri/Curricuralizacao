package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Mapeia a classe como uma entidade persistente no banco de dados gerenciada pelo JPA/Hibernate.
@Entity
// Lombok: Gera automaticamente os métodos getters e setters de todos os atributos.
@Getter @Setter
// Lombok: Gera o construtor vazio obrigatório para instanciação pelo Hibernate.
@NoArgsConstructor
public class Pais {

    // Define o campo identificador como chave primária (Primary Key) da tabela.
    @Id
    // Configura o valor do identificador para ser auto-incrementado pelo banco de dados.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Especifica explicitamente o nome da coluna no banco como "idPais".
    @Column(name = "idPais")
    private Long id; // Identificador único do país (tipo Long para suportar alta volumetria de registros).

    // Bean Validation: Garante que o campo não pode ser nulo antes de persistir no banco de dados.
    @NotNull
    // Bean Validation: Limita o texto entre 1 e 50 caracteres com mensagem de erro personalizada.
    @Size(min = 1, max = 50, message = "a descrição deve ter entre 1 a 50 carateres")
    private String descricao; // Nome ou descrição do país (ex: "Brasil").
}
