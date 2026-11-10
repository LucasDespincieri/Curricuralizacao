package com.example.curriruralizacao.controller;


import com.example.curriruralizacao.DTO.NovoComentarioForm;
import com.example.curriruralizacao.DTO.NovoPostForm;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.service.PostService;
import com.example.curriruralizacao.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/feed")
public class FeedController {

    private final PostService postService;
    private final UsuarioService usuarioService;

    // Injeção de dependência obrigatória por construtor
    public FeedController(PostService postService, UsuarioService usuarioService) {
        this.postService = postService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String carregarFeed(Model model, Principal principal) {
        model.addAttribute("posts", postService.listarFeed());

        if (!model.containsAttribute("novoPostForm")) {
            model.addAttribute("novoPostForm", new NovoPostForm());
        }
        if (!model.containsAttribute("novoComentarioForm")) {
            model.addAttribute("novoComentarioForm", new NovoComentarioForm());
        }

        if (principal != null) {
            Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
            model.addAttribute("usuarioLogado", usuarioLogado);
        }

        return "feed/index";
    }

    @PostMapping("/posts/novo")
    public String criarPost(@Valid @ModelAttribute("novoPostForm") NovoPostForm form,
                            BindingResult bindingResult,
                            Principal principal,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("posts", postService.listarFeed());
            model.addAttribute("usuarioLogado", usuarioLogado);
            if (!model.containsAttribute("novoComentarioForm")) {
                model.addAttribute("novoComentarioForm", new NovoComentarioForm());
            }
            return "feed/index";
        }

        postService.criarPost(form, usuarioLogado);
        redirectAttributes.addFlashAttribute("alertaSucesso", "Mensagem publicada com sucesso no feed da igreja!");
        return "redirect:/feed";
    }

    @PostMapping("/posts/{id}/comentar")
    public String adicionarComentario(@PathVariable("id") Long id,
                                      @Valid @ModelAttribute("novoComentarioForm") NovoComentarioForm form,
                                      BindingResult bindingResult,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("alertaErroComentario", "O comentário não pode ser vazio.");
            return "redirect:/feed#post-" + id;
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
        postService.adicionarComentario(id, form, usuarioLogado);
        return "redirect:/feed#post-" + id;
    }

    @PostMapping("/posts/{id}/excluir")
    public String excluirPost(@PathVariable("id") Long id,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
        try {
            postService.excluirPost(id, usuarioLogado);
            redirectAttributes.addFlashAttribute("alertaSucesso", "Postagem excluída com sucesso.");
        } catch (SecurityException ex) {
            redirectAttributes.addFlashAttribute("alertaErro", ex.getMessage());
        }

        return "redirect:/feed";
    }

    @PostMapping("/posts/{id}/reagir")
    public String alternarReacao(@PathVariable("id") Long id,
                                 @RequestParam("tipo") String tipo,
                                 Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
        postService.alternarReacao(id, usuarioLogado, tipo);
        return "redirect:/feed#post-" + id;
    }
}
