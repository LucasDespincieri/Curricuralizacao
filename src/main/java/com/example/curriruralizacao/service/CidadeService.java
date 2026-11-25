package com.example.curriruralizacao.service;

import com.example.curriruralizacao.DTO.CidadeForm;
import com.example.curriruralizacao.model.Cidade;
import com.example.curriruralizacao.repository.CidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service: Camada de regras de negócio para o CRUD completo da entidade cadastral Cidade.
@Service
public class CidadeService {

    private final CidadeRepository cidadeRepository;

    public CidadeService(CidadeRepository cidadeRepository) {
        this.cidadeRepository = cidadeRepository;
    }

    // Operação Read (com suporte a formulário de filtro dinâmico):
    @Transactional(readOnly = true)
    public List<Cidade> listarTodas(String busca) {
        if (busca != null && !busca.trim().isEmpty()) {
            return cidadeRepository.findByDescricaoContainingIgnoreCaseOrderByDescricaoAsc(busca.trim());
        }
        return cidadeRepository.findAllByOrderByDescricaoAsc();
    }

    // Operação Read por ID:
    @Transactional(readOnly = true)
    public Cidade buscarPorId(Integer id) {
        return cidadeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cidade não encontrada para o ID: " + id));
    }

    // Operação Create (Inserção):
    @Transactional
    public Cidade criarCidade(CidadeForm form) {
        Cidade cidade = new Cidade();
        cidade.setDescricao(form.getDescricao().trim());
        return cidadeRepository.save(cidade);
    }

    // Operação Update (Atualização/Edição):
    @Transactional
    public Cidade atualizarCidade(Integer id, CidadeForm form) {
        Cidade cidade = buscarPorId(id);
        cidade.setDescricao(form.getDescricao().trim());
        return cidadeRepository.save(cidade);
    }

    // Operação Delete (Exclusão):
    @Transactional
    public void excluirCidade(Integer id) {
        Cidade cidade = buscarPorId(id);
        cidadeRepository.delete(cidade);
    }
}
