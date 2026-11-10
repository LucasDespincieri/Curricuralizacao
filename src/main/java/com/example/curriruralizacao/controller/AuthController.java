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

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/")
    public String redirecionarInicio() {
        return "redirect:/feed";
    }

    @GetMapping("/login")
    public String telaLogin(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "sucesso", required = false) String sucesso,
                            Model model) {
        if (error != null) {
            model.addAttribute("alertaErro", "E-mail ou senha inválidos. Por favor, tente novamente.");
        }
        if (logout != null) {
            model.addAttribute("alertaSucesso", "Você encerrou sua sessão com sucesso.");
        }
        if (sucesso != null) {
            model.addAttribute("alertaSucesso", "Cadastro realizado com sucesso! Faça login para continuar.");
        }
        return "auth/login";
    }

    @GetMapping("/cadastro")
    public String telaCadastro(Model model) {
        if (!model.containsAttribute("cadastroForm")) {
            model.addAttribute("cadastroForm", new CadastroForm());
        }
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String processarCadastro(@Valid @ModelAttribute("cadastroForm") CadastroForm form,
                                    BindingResult bindingResult,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/cadastro";
        }

        try {
            usuarioService.cadastrarUsuario(form);
            return "redirect:/login?sucesso=true";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("email", "email.duplicado", ex.getMessage());
            return "auth/cadastro";
        }
    }
}
