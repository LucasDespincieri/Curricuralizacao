package com.example.curriruralizacao.controller;

import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

// @Controller: Controlador MVC responsável pela área pessoal do usuário logado (estante de livros favoritos e preferências).
// @RequestMapping("/perfil"): Prefixo de rota para recursos do perfil do membro.
@Controller
@RequestMapping("/perfil")
public class PerfilController {

    // Serviço de usuário injetado via construtor.
    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Endpoint GET /perfil/meus-livros: Renderiza a tela da biblioteca particular com os livros favoritados pelo membro.
    @GetMapping("/meus-livros")
    public String meusLivros(Model model, Principal principal) {
        // Exige usuário autenticado para acessar a área de perfil.
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
        model.addAttribute("usuarioLogado", usuarioLogado);
        // Recupera do banco a lista de livros que o membro adicionou aos seus favoritos.
        model.addAttribute("livrosFavoritos", usuarioService.listarLivrosFavoritos(usuarioLogado.getId()));

        // Renderiza o template Thymeleaf localizado em src/main/resources/templates/perfil/meus-livros.html.
        return "perfil/meus-livros";
    }

    // Endpoint POST /perfil/meus-livros/adicionar: Adiciona um livro do catálogo público à estante pessoal do usuário.
    @PostMapping("/meus-livros/adicionar")
    public String adicionarLivro(@RequestParam("livroId") Long livroId,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
        usuarioService.adicionarLivroABiblioteca(livroId, usuarioLogado.getId());

        redirectAttributes.addFlashAttribute("alertaSucesso", "Livro adicionado à sua biblioteca pessoal!");
        return "redirect:/perfil/meus-livros";
    }

    // Endpoint POST /perfil/meus-livros/remover: Remove um livro da estante de favoritos do membro.
    @PostMapping("/meus-livros/remover")
    public String removerLivro(@RequestParam("livroId") Long livroId,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
        usuarioService.removerLivroDaBiblioteca(livroId, usuarioLogado.getId());

        redirectAttributes.addFlashAttribute("alertaSucesso", "Livro removido da sua biblioteca pessoal.");
        return "redirect:/perfil/meus-livros";
    }
}
