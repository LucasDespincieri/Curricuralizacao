package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class NovoGrifoForm {

    @NotNull(message = "O capítulo referenciado é obrigatório")
    private Long capituloId;

    @NotBlank(message = "O trecho selecionado para grifo não pode estar vazio")
    private String textoSelecionado;

    private String anotacao;

    public NovoGrifoForm(Long capituloId, String textoSelecionado, String anotacao) {
        this.capituloId = capituloId;
        this.textoSelecionado = textoSelecionado;
        this.anotacao = anotacao;
    }
}
