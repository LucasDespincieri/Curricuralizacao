package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class NovoComentarioForm {

    @NotBlank(message = "O comentário não pode estar vazio")
    @Size(min = 1, max = 1000, message = "O comentário deve ter no máximo 1000 caracteres")
    private String conteudo;

    public NovoComentarioForm(String conteudo) {
        this.conteudo = conteudo;
    }
}
