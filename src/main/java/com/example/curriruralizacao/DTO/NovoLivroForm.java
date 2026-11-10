package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO para transportar e validar os dados de cadastro de uma nova obra/livro no painel administrativo do pastor.
@Getter @Setter
// Lombok: Gera o construtor padrão sem parâmetros exigido pelo framework para data binding.
@NoArgsConstructor
public class NovoLivroForm {

    // Garante que o título do livro seja preenchido e não contenha apenas espaços.
    @NotBlank(message = "O título do livro é obrigatório")
    // Valida que o título possua tamanho compreendido entre 2 e 200 caracteres.
    @Size(min = 2, max = 200, message = "O título deve ter entre 2 e 200 caracteres")
    private String titulo; // Título da publicação/livro.

}
