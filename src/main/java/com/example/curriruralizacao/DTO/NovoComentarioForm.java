package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// DTO para transportar e validar o texto enviado por um usuário ao comentar em uma publicação do feed.
@Getter @Setter
// Lombok: Gera representação em texto dos campos para facilitar logs e depuração.
@ToString
// Lombok: Construtor sem argumentos para permitir data binding pelo framework Spring MVC.
@NoArgsConstructor
public class NovoComentarioForm {

    // Impede o envio de comentários em branco ou contendo apenas espaços.
    @NotBlank(message = "O comentário não pode estar vazio")
    // Define um limite de até 1000 caracteres para manter as respostas no feed concisas e legíveis.
    @Size(min = 1, max = 1000, message = "O comentário deve ter no máximo 1000 caracteres")
    private String conteudo; // Texto digitado pelo membro da comunidade.

    // Construtor auxiliar para instanciação rápida com o conteúdo do comentário.
    public NovoComentarioForm(String conteudo) {
        this.conteudo = conteudo;
    }
}
