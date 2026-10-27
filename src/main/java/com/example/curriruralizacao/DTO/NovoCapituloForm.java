package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class NovoCapituloForm {

    @NotNull(message = "A ordem do capítulo é obrigatória")
    @Min(value = 1, message = "A ordem deve ser maior ou igual a 1")
    private Integer ordem;

    @NotBlank(message = "O título do capítulo é obrigatório")
    @Size(min = 1, max = 200, message = "O título deve ter no máximo 200 caracteres")
    private String titulo;

    @NotBlank(message = "O conteúdo do capítulo não pode estar vazio")
    private String conteudo;

    public NovoCapituloForm(Integer ordem, String titulo, String conteudo) {
        this.ordem = ordem;
        this.titulo = titulo;
        this.conteudo = conteudo;
    }
}

