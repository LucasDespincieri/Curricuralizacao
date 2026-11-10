package com.example.curriruralizacao.controller;

import com.example.curriruralizacao.DTO.CadastroForm;
import com.example.curriruralizacao.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// @Controller: Anotação do Spring MVC que define esta classe como um controlador web responsável por responder a requisições HTTP e renderizar telas HTML.
@Controller
public class AuthController {

    // Serviço de regras de negócio de usuário injetado via construtor.
    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Endpoint da raiz ("/") da aplicação: redireciona o visitante automaticamente para a página principal do feed.
    @GetMapping("/")
    public String redirecionarInicio() {
        return "redirect:/feed";
    }

    // Endpoint GET para renderizar a página de login (/login).
    // Captura parâmetros opcionais de URL (error, logout, sucesso) para exibir mensagens visuais informativas ao usuário.
    @GetMapping("/login")
    public String telaLogin(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "sucesso", required = false) String sucesso,
                            Model model) {
        // Se a tentativa de autenticação falhou, adiciona alerta de erro no modelo da view.
        if (error != null) {
            model.addAttribute("alertaErro", "E-mail ou senha inválidos. Por favor, tente novamente.");
        }
        // Se o usuário clicou em sair da conta, exibe confirmação de encerramento de sessão.
        if (logout != null) {
            model.addAttribute("alertaSucesso", "Você encerrou sua sessão com sucesso.");
        }
        // Se o usuário acabou de se cadastrar, informa que a conta foi criada e orienta a fazer login.
        if (sucesso != null) {
            model.addAttribute("alertaSucesso", "Cadastro realizado com sucesso! Faça login para continuar.");
        }
        // Retorna o template Thymeleaf localizado em src/main/resources/templates/auth/login.html.
        return "auth/login";
    }

    // Endpoint GET para exibir a tela de cadastro de novos membros (/cadastro).
    @GetMapping("/cadastro")
    public String telaCadastro(Model model) {
        // Se o modelo ainda não contém o formulário (ex: primeira visita à página), inicializa um objeto vazio para o binding.
        if (!model.containsAttribute("cadastroForm")) {
            model.addAttribute("cadastroForm", new CadastroForm());
        }
        // Retorna o template Thymeleaf localizado em src/main/resources/templates/auth/cadastro.html.
        return "auth/cadastro";
    }

    // Endpoint POST para processar a submissão dos dados do formulário de cadastro.
    // @Valid: Executa as anotações de validação declaradas no CadastroForm (@NotBlank, @Email, @Size).
    // BindingResult: Armazena os eventuais erros de validação encontrados pelo Spring.
    @PostMapping("/cadastro")
    public String processarCadastro(@Valid @ModelAttribute("cadastroForm") CadastroForm form,
                                    BindingResult bindingResult,
                                    Model model) {
        // Caso ocorra qualquer erro de validação nos campos (ex: e-mail inválido, senha curta), reapresenta o formulário com as mensagens.
        if (bindingResult.hasErrors()) {
            return "auth/cadastro";
        }

        try {
            // Invoca o serviço de negócio para criar o usuário e salvar a senha criptografada.
            usuarioService.cadastrarUsuario(form);
            // Em caso de sucesso, redireciona para a tela de login exibindo a mensagem de sucesso.
            return "redirect:/login?sucesso=true";
        } catch (IllegalArgumentException ex) {
            // Se o serviço acusar que o e-mail já existe, associa a mensagem de erro diretamente ao campo 'email' do formulário.
            bindingResult.rejectValue("email", "email.duplicado", ex.getMessage());
            return "auth/cadastro";
        }
    }
}
