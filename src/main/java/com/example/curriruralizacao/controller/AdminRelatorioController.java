package com.example.curriruralizacao.controller;

import com.example.curriruralizacao.DTO.RelatorioAgrupamentoDTO;
import com.example.curriruralizacao.DTO.RelatorioPivotDTO;
import com.example.curriruralizacao.DTO.RelatorioTotalizacaoDTO;
import com.example.curriruralizacao.service.RelatorioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping({"/admin/relatorios", "/relatorios"})
public class AdminRelatorioController {

    private final RelatorioService relatorioService;

    public AdminRelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    // Endpoint GET /admin/relatorios: Gera a visualização gráfica em Tabelas HTML com os 3 relatórios analíticos
    @GetMapping
    public String exibirRelatorios(Model model) {
        // 1. Dados da consulta em múltiplas tabelas com totalização
        model.addAttribute("relatorioTotalizacao", relatorioService.gerarRelatorioTotalizacaoMultiplasTabelas());

        // 2. Dados da consulta de agrupamento (GROUP BY)
        model.addAttribute("relatorioAgrupamento", relatorioService.gerarRelatorioAgrupamento());

        // 3. Dados da consulta de Pivot (Crosstab)
        model.addAttribute("relatorioPivot", relatorioService.gerarRelatorioPivot());

        return "admin/relatorios";
    }

    // Endpoint REST JSON para a Consulta de Múltiplas Tabelas com Totalização
    @GetMapping("/api/totalizacao")
    @ResponseBody
    public ResponseEntity<List<RelatorioTotalizacaoDTO>> apiTotalizacao() {
        return ResponseEntity.ok(relatorioService.gerarRelatorioTotalizacaoMultiplasTabelas());
    }

    // Endpoint REST JSON para a Consulta de Agrupamento
    @GetMapping("/api/agrupamento")
    @ResponseBody
    public ResponseEntity<List<RelatorioAgrupamentoDTO>> apiAgrupamento() {
        return ResponseEntity.ok(relatorioService.gerarRelatorioAgrupamento());
    }

    // Endpoint REST JSON para a Consulta de Pivot
    @GetMapping("/api/pivot")
    @ResponseBody
    public ResponseEntity<List<RelatorioPivotDTO>> apiPivot() {
        return ResponseEntity.ok(relatorioService.gerarRelatorioPivot());
    }
}
