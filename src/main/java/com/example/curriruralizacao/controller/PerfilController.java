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

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    // Injeção de dependência obrigatória por construtor
    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/meus-livros")
    public String meusLivros(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("livrosFavoritos", usuarioService.listarLivrosFavoritos(usuarioLogado.getId()));

        return "perfil/meus-livros";
    }

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
