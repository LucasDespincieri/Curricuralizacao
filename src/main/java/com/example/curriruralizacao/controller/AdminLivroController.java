package com.example.curriruralizacao.controller;

import com.example.curriruralizacao.DTO.EdicaoCapituloForm;
import com.example.curriruralizacao.DTO.EdicaoLivroForm;
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

// @Controller: Controlador MVC administrativo exclusivo para gerenciamento de livros e capítulos pelo Pastor.
// @RequestMapping("/admin"): Prefixo de rota de acesso administrativo.
// @PreAuthorize("hasAnyRole('ADM', 'ADMIN')"): Proteção a nível de classe garantindo que apenas usuários com papel de Administrador/Pastor acessem estes métodos.
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADM', 'ADMIN')")
public class AdminLivroController {

    // Injeção de dependências via construtor dos serviços de livros e usuários.
    private final LivroService livroService;
    private final UsuarioService usuarioService;

    public AdminLivroController(LivroService livroService, UsuarioService usuarioService) {
        this.livroService = livroService;
        this.usuarioService = usuarioService;
    }

    // Endpoint GET /admin/livros/novo: Renderiza a tela de cadastro de uma nova obra.
    @GetMapping("/livros/novo")
    public String telaNovoLivro(Model model) {
        if (!model.containsAttribute("novoLivroForm")) {
            model.addAttribute("novoLivroForm", new NovoLivroForm());
        }
        return "admin/livro-form";
    }

    // Endpoint POST /admin/livros/novo: Salva o novo livro e direciona para a criação do primeiro capítulo.
    @PostMapping("/livros/novo")
    public String salvarNovoLivro(@Valid @ModelAttribute("novoLivroForm") NovoLivroForm form,
                                  BindingResult bindingResult,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        // Se houver erro de validação (ex: título muito curto ou vazio), reapresenta o formulário.
        if (bindingResult.hasErrors()) {
            return "admin/livro-form";
        }

        Usuario pastor = usuarioService.buscarPorEmail(principal.getName());
        Livro livro = livroService.criarLivro(form, pastor);

        redirectAttributes.addFlashAttribute("alertaSucesso", "Livro '" + livro.getTitulo() + "' cadastrado! Agora adicione os capítulos da obra.");
        return "redirect:/admin/livros/" + livro.getId() + "/capitulos/novo";
    }

    // Endpoint GET /admin/livros/{id}/capitulos/novo: Renderiza a tela para adição de um capítulo ao livro selecionado.
    @GetMapping("/livros/{id}/capitulos/novo")
    public String telaNovoCapitulo(@PathVariable("id") Long id, Model model) {
        Livro livro = livroService.buscarLivroPorId(id);
        // Calcula a próxima ordem sugerida (tamanho atual dos capítulos + 1).
        int proximaOrdem = livroService.listarCapitulosDoLivro(id).size() + 1;

        model.addAttribute("livro", livro);
        if (!model.containsAttribute("novoCapituloForm")) {
            model.addAttribute("novoCapituloForm", new NovoCapituloForm(proximaOrdem, "", ""));
        }
        return "admin/capitulo-form";
    }

    // Endpoint POST /admin/livros/{id}/capitulos/novo: Processa o salvamento do novo capítulo no livro.
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
            // Se o número de ordem já estiver em uso, vincula o erro ao campo 'ordem' no formulário.
            model.addAttribute("livro", livroService.buscarLivroPorId(id));
            bindingResult.rejectValue("ordem", "ordem.duplicada", ex.getMessage());
            return "admin/capitulo-form";
        }
    }

    // Endpoint POST /admin/livros/{id}/excluir: Remove um livro e todos os seus capítulos em cascata.
    @PostMapping("/livros/{id}/excluir")
    public String excluirLivro(@PathVariable("id") Long id,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        Usuario pastor = usuarioService.buscarPorEmail(principal.getName());
        livroService.excluirLivro(id, pastor);
        redirectAttributes.addFlashAttribute("alertaSucesso", "Livro e seus capítulos foram removidos com sucesso.");
        return "redirect:/admin/livros";
    }

    // Endpoint GET /admin/livros: Gera a lista de livros em formato de Tabela com formulário de filtro (REQUISITOS: Geração de Dados em Tabelas + Formulário de Filtro)
    @GetMapping("/livros")
    public String listarLivrosTabela(@RequestParam(value = "busca", required = false) String busca, Model model) {
        model.addAttribute("livros", livroService.filtrarLivros(busca));
        model.addAttribute("termoBusca", busca != null ? busca : "");
        return "admin/livros-tabela";
    }

    // Endpoint GET /admin/livros/{id}/editar: Tela de edição de um livro existente (Operação Update do CRUD)
    @GetMapping("/livros/{id}/editar")
    public String telaEditarLivro(@PathVariable("id") Long id, Model model) {
        Livro livro = livroService.buscarLivroPorId(id);
        if (!model.containsAttribute("edicaoLivroForm")) {
            model.addAttribute("edicaoLivroForm", new EdicaoLivroForm(livro.getId(), livro.getTitulo()));
        }
        model.addAttribute("livro", livro);
        return "admin/livro-editar";
    }

    // Endpoint POST /admin/livros/{id}/editar: Processa a atualização dos dados do livro (Operação Update do CRUD)
    @PostMapping("/livros/{id}/editar")
    public String salvarEdicaoLivro(@PathVariable("id") Long id,
                                    @Valid @ModelAttribute("edicaoLivroForm") EdicaoLivroForm form,
                                    BindingResult bindingResult,
                                    Principal principal,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("livro", livroService.buscarLivroPorId(id));
            return "admin/livro-editar";
        }
        Usuario pastor = usuarioService.buscarPorEmail(principal.getName());
        livroService.atualizarLivro(id, form, pastor);
        redirectAttributes.addFlashAttribute("alertaSucesso", "Livro atualizado com sucesso!");
        return "redirect:/admin/livros";
    }

    // Endpoint GET /admin/livros/{livroId}/capitulos/{capituloId}/editar: Tela de edição de capítulo (Operação Update do CRUD)
    @GetMapping("/livros/{livroId}/capitulos/{capituloId}/editar")
    public String telaEditarCapitulo(@PathVariable("livroId") Long livroId,
                                     @PathVariable("capituloId") Long capituloId,
                                     Model model) {
        Livro livro = livroService.buscarLivroPorId(livroId);
        com.example.curriruralizacao.model.Capitulo capitulo = livroService.buscarCapituloPorId(capituloId);
        if (!model.containsAttribute("edicaoCapituloForm")) {
            model.addAttribute("edicaoCapituloForm", new EdicaoCapituloForm(capitulo.getId(), capitulo.getOrdem(), capitulo.getTitulo(), capitulo.getConteudo()));
        }
        model.addAttribute("livro", livro);
        model.addAttribute("capitulo", capitulo);
        return "admin/capitulo-editar";
    }

    // Endpoint POST /admin/livros/{livroId}/capitulos/{capituloId}/editar: Processa a edição de capítulo (Operação Update do CRUD)
    @PostMapping("/livros/{livroId}/capitulos/{capituloId}/editar")
    public String salvarEdicaoCapitulo(@PathVariable("livroId") Long livroId,
                                       @PathVariable("capituloId") Long capituloId,
                                       @Valid @ModelAttribute("edicaoCapituloForm") EdicaoCapituloForm form,
                                       BindingResult bindingResult,
                                       Principal principal,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("livro", livroService.buscarLivroPorId(livroId));
            model.addAttribute("capitulo", livroService.buscarCapituloPorId(capituloId));
            return "admin/capitulo-editar";
        }
        Usuario pastor = usuarioService.buscarPorEmail(principal.getName());
        try {
            livroService.atualizarCapitulo(capituloId, form, pastor);
            redirectAttributes.addFlashAttribute("alertaSucesso", "Capítulo atualizado com sucesso!");
            return "redirect:/livros/" + livroId + "/ler?capitulo=" + form.getOrdem();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("livro", livroService.buscarLivroPorId(livroId));
            model.addAttribute("capitulo", livroService.buscarCapituloPorId(capituloId));
            bindingResult.rejectValue("ordem", "ordem.duplicada", ex.getMessage());
            return "admin/capitulo-editar";
        }
    }
}
