package com.example.curriruralizacao.controller.api;

import com.example.curriruralizacao.DTO.CidadeForm;
import com.example.curriruralizacao.model.Cidade;
import com.example.curriruralizacao.model.ENUMS.Roles;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.service.CidadeService;
import com.example.curriruralizacao.service.LocalizacaoService;
import com.example.curriruralizacao.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// @RestController: CRUD de Cidades (tabela cadastral) da área de Administração. Só o Pastor (ADM) acessa.
//   GET    /api/cidades?busca=...  → lista (com filtro pelo nome)
//   GET    /api/cidades/ufs        → siglas aceitas no formulário
//   POST   /api/cidades            → cadastra   { "nome": "Assis", "uf": "SP" }
//   PUT    /api/cidades/{id}       → edita
//   DELETE /api/cidades/{id}       → exclui (recusado se algum membro mora na cidade)
@RestController
@RequestMapping("/api/cidades")
public class ApiCidadeController {

    private final CidadeService cidadeService;
    private final UsuarioService usuarioService;

    public ApiCidadeController(CidadeService cidadeService, UsuarioService usuarioService) {
        this.cidadeService = cidadeService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(value = "busca", required = false) String busca, Principal principal) {
        ResponseEntity<?> negado = exigirPastor(principal);
        if (negado != null) return negado;
        Map<Integer, Long> membros = cidadeService.contarMembrosPorCidade();
        List<Map<String, Object>> lista = cidadeService.listarTodas(busca).stream()
                .map(c -> toDto(c, membros.getOrDefault(c.getId(), 0L)))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/ufs")
    public List<String> ufs() {
        return LocalizacaoService.UFS_BRASIL;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody CidadeForm form, Principal principal) {
        ResponseEntity<?> negado = exigirPastor(principal);
        if (negado != null) return negado;
        Cidade cidade = cidadeService.criarCidade(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(cidade, 0L));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @RequestBody CidadeForm form, Principal principal) {
        ResponseEntity<?> negado = exigirPastor(principal);
        if (negado != null) return negado;
        Cidade cidade = cidadeService.atualizarCidade(id, form);
        return ResponseEntity.ok(toDto(cidade, cidadeService.contarMembrosPorCidade().getOrDefault(id, 0L)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Integer id, Principal principal) {
        ResponseEntity<?> negado = exigirPastor(principal);
        if (negado != null) return negado;
        cidadeService.excluirCidade(id); // IllegalStateException (cidade em uso) vira HTTP 409 no ApiExceptionHandler
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    private static Map<String, Object> toDto(Cidade c, long membros) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", c.getId());
        dto.put("nome", c.getDescricao());
        dto.put("uf", c.getUF() != null ? c.getUF().getUF() : "");
        dto.put("membros", membros);
        return dto;
    }

    private ResponseEntity<?> exigirPastor(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        if (user.getRole() != Roles.ADM) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Somente o Pastor pode gerenciar cidades."));
        }
        return null;
    }
}
