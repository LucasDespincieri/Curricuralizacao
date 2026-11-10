package com.example.curriruralizacao.controller;

import com.example.curriruralizacao.DTO.NovoGrifoForm;
import com.example.curriruralizacao.model.Capitulo;
import com.example.curriruralizacao.model.Livro;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.service.GrifoService;
import com.example.curriruralizacao.service.LivroService;
import com.example.curriruralizacao.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class BibliotecaController {

    private final LivroService livroService;
    private final GrifoService grifoService;
    private final UsuarioService usuarioService;

    public BibliotecaController(LivroService livroService,
                                GrifoService grifoService,
                                UsuarioService usuarioService) {
        this.livroService = livroService;
        this.grifoService = grifoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/biblioteca")
    public String catalogo(Model model, Principal principal) {
        model.addAttribute("livros", livroService.listarTodosLivros());

        if (principal != null) {
            Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
            model.addAttribute("usuarioLogado", usuarioLogado);
            Set<Long> favoritosIds = usuarioLogado.getLivrosFavoritos().stream()
                    .map(Livro::getId)
                    .collect(Collectors.toSet());
            model.addAttribute("meusLivrosIds", favoritosIds);
        }

        return "biblioteca/catalogo";
    }

    @GetMapping("/livros/{id}/ler")
    public String leitorCapitulo(@PathVariable("id") Long id,
                                 @RequestParam(value = "capitulo", defaultValue = "1") Integer ordem,
                                 Model model,
                                 Principal principal) {
        Livro livro = livroService.buscarLivroPorId(id);
        Capitulo capituloAtual = livroService.buscarCapituloPorOrdem(id, ordem);
        List<Capitulo> todosCapitulos = livroService.listarCapitulosDoLivro(id);

        model.addAttribute("livro", livro);
        model.addAttribute("capituloAtual", capituloAtual);
        model.addAttribute("capitulos", todosCapitulos);

        if (principal != null) {
            Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
            model.addAttribute("usuarioLogado", usuarioLogado);


            model.addAttribute("grifos", grifoService.listarGrifosDoCapitulo(capituloAtual.getId(), usuarioLogado.getId()));

            if (!model.containsAttribute("novoGrifoForm")) {
                model.addAttribute("novoGrifoForm", new NovoGrifoForm(capituloAtual.getId(), "", ""));
            }
        }

        return "biblioteca/leitor";
    }

    @PostMapping("/livros/{id}/grifar")
    public String salvarGrifo(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("novoGrifoForm") NovoGrifoForm form,
                              BindingResult bindingResult,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("alertaErro", "O texto selecionado para o grifo não pode estar vazio.");
            return "redirect:/livros/" + id + "/ler";
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
        grifoService.salvarGrifo(form, usuarioLogado);

        Capitulo capitulo = livroService.buscarCapituloPorOrdem(id, 1);
        redirectAttributes.addFlashAttribute("alertaSucesso", "Grifo salvo com sucesso em suas reflexões particulares.");
        return "redirect:/livros/" + id + "/ler?capitulo=" + capitulo.getOrdem();
    }
}
