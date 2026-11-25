package com.example.curriruralizacao.service;

import com.example.curriruralizacao.DTO.RelatorioAgrupamentoDTO;
import com.example.curriruralizacao.DTO.RelatorioPivotDTO;
import com.example.curriruralizacao.DTO.RelatorioTotalizacaoDTO;
import com.example.curriruralizacao.repository.RelatorioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RelatorioService {

    private final RelatorioRepository relatorioRepository;

    public RelatorioService(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    @Transactional(readOnly = true)
    public List<RelatorioTotalizacaoDTO> gerarRelatorioTotalizacaoMultiplasTabelas() {
        return relatorioRepository.relatorioTotalizacaoMultiplasTabelas();
    }

    @Transactional(readOnly = true)
    public List<RelatorioAgrupamentoDTO> gerarRelatorioAgrupamento() {
        return relatorioRepository.relatorioAgrupamentoAutoresLivros();
    }

    @Transactional(readOnly = true)
    public List<RelatorioPivotDTO> gerarRelatorioPivot() {
        return relatorioRepository.relatorioPivotReacoesPorPost();
    }
}
