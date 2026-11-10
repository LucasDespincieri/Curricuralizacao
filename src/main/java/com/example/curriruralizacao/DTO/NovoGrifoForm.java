package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO para transportar a seleção de texto e reflexão pessoal (grifo) feitas pelo leitor em um capítulo.
@Getter @Setter
// Lombok: Gera o construtor vazio obrigatório para o Spring MVC mapear parâmetros do formulário.
@NoArgsConstructor
public class NovoGrifoForm {

    // Identificador obrigatório do capítulo onde o destaque foi efetuado.
    @NotNull(message = "O capítulo referenciado é obrigatório")
    private Long capituloId; // ID da entidade Capitulo associada.

    // Garante que o usuário realmente selecionou um trecho de texto antes de salvar.
    @NotBlank(message = "O trecho selecionado para grifo não pode estar vazio")
    private String textoSelecionado; // Trecho marcado/destacado pelo leitor.

    // Campo opcional para anotação, oração ou reflexão pessoal do participante sobre a passagem.
    private String anotacao; // Nota pessoal do usuário.

    // Construtor completo para inicialização de grifos em formulários e controladores.
    public NovoGrifoForm(Long capituloId, String textoSelecionado, String anotacao) {
        this.capituloId = capituloId;
        this.textoSelecionado = textoSelecionado;
        this.anotacao = anotacao;
    }
}
