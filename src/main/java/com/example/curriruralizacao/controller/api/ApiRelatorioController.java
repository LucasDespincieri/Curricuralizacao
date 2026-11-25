package com.example.curriruralizacao.controller.api;

import com.example.curriruralizacao.model.ENUMS.Roles;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.service.RelatorioService;
import com.example.curriruralizacao.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

// @RestController: Relatórios gerenciais da área de Administração. Só o Pastor (ADM) acessa,
// porque o relatório de totalização mostra o e-mail de todos os membros.
//   1. Totalização em múltiplas tabelas: posts, comentários e reações por membro (JOINs + COUNT).
//   2. Agrupamento (GROUP BY + HAVING): livros e capítulos por autor.
//   3. Pivot (crosstab): reações de cada post em colunas (Amém / Curtir) + total.
@RestController
@RequestMapping("/api/relatorios")
public class ApiRelatorioController {

    private final RelatorioService relatorioService;
    private final UsuarioService usuarioService;

    public ApiRelatorioController(RelatorioService relatorioService, UsuarioService usuarioService) {
        this.relatorioService = relatorioService;
        this.usuarioService = usuarioService;
    }

    // Os três relatórios em uma resposta só (a tela de Relatórios carrega tudo de uma vez).
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> todos(Principal principal) {
        ResponseEntity<?> negado = exigirPastor(principal);
        if (negado != null) return negado;

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("totalizacao", relatorioService.gerarRelatorioTotalizacaoMultiplasTabelas());
        dados.put("agrupamento", relatorioService.gerarRelatorioAgrupamento());
        dados.put("pivot", relatorioService.gerarRelatorioPivot());
        return ResponseEntity.ok(dados);
    }

    @GetMapping("/totalizacao")
    @Transactional(readOnly = true)
    public ResponseEntity<?> totalizacao(Principal principal) {
        ResponseEntity<?> negado = exigirPastor(principal);
        return negado != null ? negado : ResponseEntity.ok(relatorioService.gerarRelatorioTotalizacaoMultiplasTabelas());
    }

    @GetMapping("/agrupamento")
    @Transactional(readOnly = true)
    public ResponseEntity<?> agrupamento(Principal principal) {
        ResponseEntity<?> negado = exigirPastor(principal);
        return negado != null ? negado : ResponseEntity.ok(relatorioService.gerarRelatorioAgrupamento());
    }

    @GetMapping("/pivot")
    @Transactional(readOnly = true)
    public ResponseEntity<?> pivot(Principal principal) {
        ResponseEntity<?> negado = exigirPastor(principal);
        return negado != null ? negado : ResponseEntity.ok(relatorioService.gerarRelatorioPivot());
    }

    // Devolve a resposta de erro quando quem chamou não é o Pastor; null quando pode seguir.
    private ResponseEntity<?> exigirPastor(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        if (user.getRole() != Roles.ADM) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Somente o Pastor pode ver os relatórios."));
        }
        return null;
    }
}
