package com.example.curriruralizacao.controller;

import com.example.curriruralizacao.DTO.CidadeForm;
import com.example.curriruralizacao.model.Cidade;
import com.example.curriruralizacao.service.CidadeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// @Controller: Controlador administrativo responsável pelo CRUD completo da tabela cadastral Cidade.
// @PreAuthorize: Protegido exclusivamente para usuários com papel administrativo (Pastor/ADM).
@Controller
@RequestMapping("/admin/cidades")
@PreAuthorize("hasAnyRole('ADM', 'ADMIN')")
public class AdminCidadeController {

    private final CidadeService cidadeService;

    public AdminCidadeController(CidadeService cidadeService) {
        this.cidadeService = cidadeService;
    }

    // Endpoint GET /admin/cidades: Exibe a lista de cidades em formato de Tabela HTML com formulário de filtro dinâmico
    @GetMapping
    public String listarCidades(@RequestParam(value = "busca", required = false) String busca, Model model) {
        model.addAttribute("cidades", cidadeService.listarTodas(busca));
        model.addAttribute("termoBusca", busca != null ? busca : "");
        if (!model.containsAttribute("novaCidadeForm")) {
            model.addAttribute("novaCidadeForm", new CidadeForm());
        }
        return "admin/cidades";
    }

    // Endpoint POST /admin/cidades/novo: Cadastra uma nova cidade no banco de dados (Operação Create do CRUD)
    @PostMapping("/novo")
    public String salvarCidade(@Valid @ModelAttribute("novaCidadeForm") CidadeForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("cidades", cidadeService.listarTodas(null));
            return "admin/cidades";
        }
        cidadeService.criarCidade(form);
        redirectAttributes.addFlashAttribute("alertaSucesso", "Cidade '" + form.getDescricao() + "' cadastrada com sucesso!");
        return "redirect:/admin/cidades";
    }

    // Endpoint GET /admin/cidades/{id}/editar: Tela para edição da cidade selecionada (Operação Update do CRUD)
    @GetMapping("/{id}/editar")
    public String telaEditarCidade(@PathVariable("id") Integer id, Model model) {
        Cidade cidade = cidadeService.buscarPorId(id);
        if (!model.containsAttribute("edicaoCidadeForm")) {
            model.addAttribute("edicaoCidadeForm", new CidadeForm(cidade.getId(), cidade.getDescricao()));
        }
        model.addAttribute("cidade", cidade);
        return "admin/cidade-editar";
    }

    // Endpoint POST /admin/cidades/{id}/editar: Processa a atualização da cidade (Operação Update do CRUD)
    @PostMapping("/{id}/editar")
    public String atualizarCidade(@PathVariable("id") Integer id,
                                  @Valid @ModelAttribute("edicaoCidadeForm") CidadeForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("cidade", cidadeService.buscarPorId(id));
            return "admin/cidade-editar";
        }
        cidadeService.atualizarCidade(id, form);
        redirectAttributes.addFlashAttribute("alertaSucesso", "Cidade atualizada com sucesso!");
        return "redirect:/admin/cidades";
    }

    // Endpoint POST /admin/cidades/{id}/excluir: Exclui a cidade do banco de dados (Operação Delete do CRUD)
    @PostMapping("/{id}/excluir")
    public String excluirCidade(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        cidadeService.excluirCidade(id);
        redirectAttributes.addFlashAttribute("alertaSucesso", "Cidade excluída com sucesso!");
        return "redirect:/admin/cidades";
    }
}
