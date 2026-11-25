package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO para transportar e validar os dados de atualização/edição de um Livro existente (Operação Update do CRUD).
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class EdicaoLivroForm {

    private Long id; // Identificador primário do livro a ser atualizado

    @NotBlank(message = "O título do livro é obrigatório")
    @Size(min = 2, max = 200, message = "O título deve ter entre 2 e 200 caracteres")
    private String titulo; // Título atualizado da obra
}
