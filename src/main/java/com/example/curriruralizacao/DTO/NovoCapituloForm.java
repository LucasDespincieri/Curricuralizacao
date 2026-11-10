package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO (Data Transfer Object) para transportar e validar os dados de cadastro de um novo capítulo de livro pelo pastor.
@Getter @Setter
// Lombok: Gera o construtor vazio exigido pelo Spring MVC para efetuar o binding das requisições HTTP.
@NoArgsConstructor
public class NovoCapituloForm {

    // Garante que o número de ordem seja informado obrigatoriamente.
    @NotNull(message = "A ordem do capítulo é obrigatória")
    // Valida que a ordem seja um número positivo (a partir do capítulo 1).
    @Min(value = 1, message = "A ordem deve ser maior ou igual a 1")
    private Integer ordem; // Posição/ordem numérica do capítulo no livro (ex: 1, 2, 3...).

    // Impede que o título do capítulo seja nulo ou composto apenas de espaços.
    @NotBlank(message = "O título do capítulo é obrigatório")
    // Limita o título em até 200 caracteres, conforme a capacidade da coluna na tabela de capítulos.
    @Size(min = 1, max = 200, message = "O título deve ter no máximo 200 caracteres")
    private String titulo; // Título ou subtítulo do capítulo.

    // Garante que o texto integral do capítulo não esteja vazio.
    @NotBlank(message = "O conteúdo do capítulo não pode estar vazio")
    private String conteudo; // Corpo do capítulo (aceita textos extensos).

    // Construtor completo para facilitar instanciação parametrizada em controllers e testes.
    public NovoCapituloForm(Integer ordem, String titulo, String conteudo) {
        this.ordem = ordem;
        this.titulo = titulo;
        this.conteudo = conteudo;
    }
}
