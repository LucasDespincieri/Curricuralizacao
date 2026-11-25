package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO para transportar e validar os dados de atualização/edição de um Capítulo existente (Operação Update do CRUD).
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class EdicaoCapituloForm {

    private Long id; // Identificador primário do capítulo

    @NotNull(message = "A ordem do capítulo é obrigatória")
    @Min(value = 1, message = "A ordem deve ser maior ou igual a 1")
    private Integer ordem; // Sequência/ordem do capítulo

    @NotBlank(message = "O título do capítulo é obrigatório")
    @Size(min = 1, max = 200, message = "O título deve ter no máximo 200 caracteres")
    private String titulo; // Título do capítulo

    @NotBlank(message = "O conteúdo do capítulo não pode estar vazio")
    private String conteudo; // Conteúdo textual completo
}
