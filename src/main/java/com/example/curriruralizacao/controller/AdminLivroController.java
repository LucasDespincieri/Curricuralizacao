package com.example.curriruralizacao.controller;

import com.example.curriruralizacao.DTO.NovoCapituloForm;
import com.example.curriruralizacao.DTO.NovoLivroForm;
import com.example.curriruralizacao.model.Livro;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.service.LivroService;
import com.example.curriruralizacao.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLivroController {

    private final LivroService livroService;
    private final UsuarioService usuarioService;

    // Injeção de dependência obrigatória por construtor
    public AdminLivroController(LivroService livroService, UsuarioService usuarioService) {
        this.livroService = livroService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/livros/novo")
    public String telaNovoLivro(Model model) {
        if (!model.containsAttribute("novoLivroForm")) {
            model.addAttribute("novoLivroForm", new NovoLivroForm());
        }
        return "admin/livro-form";
    }

    @PostMapping("/livros/novo")
    public String salvarNovoLivro(@Valid @ModelAttribute("novoLivroForm") NovoLivroForm form,
                                  BindingResult bindingResult,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/livro-form";
        }

        Usuario pastor = usuarioService.buscarPorEmail(principal.getName());
        Livro livro = livroService.criarLivro(form, pastor);

        redirectAttributes.addFlashAttribute("alertaSucesso", "Livro '" + livro.getTitulo() + "' cadastrado! Agora adicione os capítulos da obra.");
        return "redirect:/admin/livros/" + livro.getId() + "/capitulos/novo";
    }

    @GetMapping("/livros/{id}/capitulos/novo")
    public String telaNovoCapitulo(@PathVariable("id") Long id, Model model) {
        Livro livro = livroService.buscarLivroPorId(id);
        int proximaOrdem = livroService.listarCapitulosDoLivro(id).size() + 1;

        model.addAttribute("livro", livro);
        if (!model.containsAttribute("novoCapituloForm")) {
            model.addAttribute("novoCapituloForm", new NovoCapituloForm(proximaOrdem, "", ""));
        }
        return "admin/capitulo-form";
    }

    @PostMapping("/livros/{id}/capitulos/novo")
    public String salvarNovoCapitulo(@PathVariable("id") Long id,
                                     @Valid @ModelAttribute("novoCapituloForm") NovoCapituloForm form,
                                     BindingResult bindingResult,
                                     Principal principal,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("livro", livroService.buscarLivroPorId(id));
            return "admin/capitulo-form";
        }

        Usuario pastor = usuarioService.buscarPorEmail(principal.getName());
        try {
            livroService.adicionarCapitulo(id, form, pastor);
            redirectAttributes.addFlashAttribute("alertaSucesso", "Capítulo " + form.getOrdem() + " adicionado com sucesso!");
            return "redirect:/livros/" + id + "/ler?capitulo=" + form.getOrdem();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("livro", livroService.buscarLivroPorId(id));
            bindingResult.rejectValue("ordem", "ordem.duplicada", ex.getMessage());
            return "admin/capitulo-form";
        }
    }

    @PostMapping("/livros/{id}/excluir")
    public String excluirLivro(@PathVariable("id") Long id,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        Usuario pastor = usuarioService.buscarPorEmail(principal.getName());
        livroService.excluirLivro(id, pastor);
        redirectAttributes.addFlashAttribute("alertaSucesso", "Livro e seus capítulos foram removidos com sucesso.");
        return "redirect:/biblioteca";
    }
}
