package com.example.curriruralizacao.service;

import com.example.curriruralizacao.DTO.RelatorioAgrupamentoDTO;
import com.example.curriruralizacao.DTO.RelatorioPivotDTO;
import com.example.curriruralizacao.DTO.RelatorioTotalizacaoDTO;
import com.example.curriruralizacao.repository.RelatorioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service: Camada de serviço responsável por fornecer os dados das consultas analíticas e relatórios.
@Service
public class RelatorioService {

    private final RelatorioRepository relatorioRepository;

    public RelatorioService(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    // REQUISITO: Consultas com dados em Múltiplas Tabelas com totalização de dados (Valor: 1,0)
    @Transactional(readOnly = true)
    public List<RelatorioTotalizacaoDTO> gerarRelatorioTotalizacaoMultiplasTabelas() {
        return relatorioRepository.relatorioTotalizacaoMultiplasTabelas();
    }

    // REQUISITO: Consulta de agrupamento (Valor: 0,5)
    @Transactional(readOnly = true)
    public List<RelatorioAgrupamentoDTO> gerarRelatorioAgrupamento() {
        return relatorioRepository.relatorioAgrupamentoAutoresLivros();
    }

    // REQUISITO: Consulta de Pivot (Valor: 0,5)
    @Transactional(readOnly = true)
    public List<RelatorioPivotDTO> gerarRelatorioPivot() {
        List<RelatorioPivotDTO> lista = relatorioRepository.relatorioPivotReacoesPorPost();
        // Sanitiza trechos muito longos para exibição adequada em tabela HTML
        for (RelatorioPivotDTO dto : lista) {
            if (dto.getPostTrecho() != null && dto.getPostTrecho().length() > 60) {
                dto.setPostTrecho(dto.getPostTrecho().substring(0, 57) + "...");
            }
        }
        return lista;
    }
}
