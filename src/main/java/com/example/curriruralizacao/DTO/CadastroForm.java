package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO (Data Transfer Object) para capturar e validar os dados da submissão do formulário de cadastro de novos membros.
@Getter @Setter
// Lombok: Gera o construtor padrão sem argumentos para permitir o bind automático do formulário pelo Spring MVC.
@NoArgsConstructor
public class CadastroForm {

    // Garante que o nome do usuário não seja nulo e nem composto apenas por espaços em branco.
    @NotBlank(message = "O nome é obrigatório")
    // Impõe um limite mínimo e máximo de caracteres para o nome do membro.
    @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
    private String nome; // Armazena o nome completo digitado no cadastro.

    // Garante que o e-mail não seja nulo ou vazio.
    @NotBlank(message = "O e-mail é obrigatório")
    // Valida se o formato do e-mail digitado respeita o padrão RFC de endereços eletrônicos (ex: usuario@dominio.com).
    @Email(message = "Informe um endereço de e-mail válido")
    // Limita o tamanho do e-mail para comportar no banco de dados.
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
    private String email; // Armazena o e-mail que servirá também como identificador de login.

    // Garante que a senha seja preenchida.
    @NotBlank(message = "A senha é obrigatória")
    // Impõe uma política de complexidade mínima de 6 caracteres para garantir a segurança da credencial.
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    private String senha; // Armazena a senha em texto puro digitada no formulário antes de ser criptografada.

    // Construtor auxiliar para facilitar testes unitários ou instanciação manual do DTO.
    public CadastroForm(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }
}