package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO para transportar e validar a mensagem de uma nova publicação a ser exibida no feed da igreja.
@Getter @Setter
// Lombok: Gera o construtor vazio necessário para o mecanismo de binding do Spring MVC.
@NoArgsConstructor
public class NovoPostForm {

    // Garante que o corpo da postagem não seja vazio ou preenchido somente com espaços.
    @NotBlank(message = "O conteúdo da postagem não pode estar vazio")
    // Define um limite seguro de até 5000 caracteres para postagens no feed.
    @Size(min = 1, max = 5000, message = "A postagem deve ter no máximo 5000 caracteres")
    private String conteudo; // Texto do depoimento, reflexão ou aviso postado pelo membro.

}
