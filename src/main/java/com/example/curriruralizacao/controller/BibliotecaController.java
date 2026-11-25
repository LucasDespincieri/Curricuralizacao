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

// @Controller: Controlador MVC responsável pelo catálogo público de obras da igreja e leitor interativo de capítulos com anotações.
@Controller
public class BibliotecaController {

    // Serviços de negócio necessários para alimentar o catálogo, leitura e destaques de texto.
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

    // Endpoint GET /biblioteca: Exibe a lista completa de livros disponíveis para leitura comunitária.
    @GetMapping("/biblioteca")
    public String catalogo(Model model, Principal principal) {
        // Envia todos os livros ordenados pela data de publicação para o catálogo.
        model.addAttribute("livros", livroService.listarTodosLivros());

        // Se o usuário estiver autenticado, identifica quais livros já estão salvos em sua biblioteca particular.
        if (principal != null) {
            Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
            model.addAttribute("usuarioLogado", usuarioLogado);
            // Coleta apenas os IDs dos livros favoritos para facilitar a checagem no template Thymeleaf (botões de adicionar/remover).
            Set<Long> favoritosIds = usuarioLogado.getLivrosFavoritos().stream()
                    .map(Livro::getId)
                    .collect(Collectors.toSet());
            model.addAttribute("meusLivrosIds", favoritosIds);
        }

        // Renderiza o template em src/main/resources/templates/biblioteca/catalogo.html.
        return "biblioteca/catalogo";
    }

    // Endpoint GET /livros/{id}/ler: Interface de leitura do capítulo de um livro.
    // Permite navegar entre capítulos via parâmetro de query "?capitulo=N" (valor padrão = 1).
    @GetMapping("/livros/{id}/ler")
    public String leitorCapitulo(@PathVariable("id") Long id,
                                 @RequestParam(value = "capitulo", defaultValue = "1") Integer ordem,
                                 Model model,
                                 Principal principal) {
        // Busca a obra, o capítulo atual pela ordem indicada e a lista com todos os capítulos para o sumário lateral.
        Livro livro = livroService.buscarLivroPorId(id);
        Capitulo capituloAtual = livroService.buscarCapituloPorOrdem(id, ordem);
        List<Capitulo> todosCapitulos = livroService.listarCapitulosDoLivro(id);

        model.addAttribute("livro", livro);
        model.addAttribute("capituloAtual", capituloAtual);
        model.addAttribute("capitulos", todosCapitulos);

        // Se autenticado, carrega as anotações e grifos particulares que o leitor já salvou neste capítulo específico.
        if (principal != null) {
            Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
            model.addAttribute("usuarioLogado", usuarioLogado);

            // Lista as marcações do usuário neste capítulo.
            model.addAttribute("grifos", grifoService.listarGrifosDoCapitulo(capituloAtual.getId(), usuarioLogado.getId()));

            // Inicializa o formulário de novo grifo caso ainda não exista no modelo.
            if (!model.containsAttribute("novoGrifoForm")) {
                model.addAttribute("novoGrifoForm", new NovoGrifoForm(capituloAtual.getId(), "", ""));
            }
        }

        // Renderiza o template do leitor em src/main/resources/templates/biblioteca/leitor.html.
        return "biblioteca/leitor";
    }

    // Endpoint POST /livros/{id}/grifar: Salva um destaque de texto e anotação pessoal feitos pelo leitor no capítulo.
    @PostMapping("/livros/{id}/grifar")
    public String salvarGrifo(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("novoGrifoForm") NovoGrifoForm form,
                              BindingResult bindingResult,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        // Requer que o usuário esteja autenticado para salvar reflexões pessoais.
        if (principal == null) {
            return "redirect:/login";
        }

        // Se o texto selecionado for vazio, redireciona de volta informando o erro.
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
