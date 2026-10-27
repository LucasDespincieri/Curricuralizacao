package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
public class NovoPostForm {

    @NotBlank(message = "O conteúdo da postagem não pode estar vazio")
    @Size(min = 1, max = 5000, message = "A postagem deve ter no máximo 5000 caracteres")
    private String conteudo;

}


