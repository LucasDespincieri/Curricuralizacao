package com.example.curriruralizacao.service;

import com.example.curriruralizacao.DTO.CidadeForm;
import com.example.curriruralizacao.model.Cidade;
import com.example.curriruralizacao.model.UF;
import com.example.curriruralizacao.repository.CidadeRepository;
import com.example.curriruralizacao.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Regras de negócio do CRUD de Cidades (tabela cadastral), usado pela tela de Administração do Pastor.
// Uma cidade é sempre "nome + UF": não pode haver duas "Assis/SP", mas pode existir "Assis/SP" e "Assis/MG".
@Service
public class CidadeService {

    private final CidadeRepository cidadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final LocalizacaoService localizacaoService;

    public CidadeService(CidadeRepository cidadeRepository, UsuarioRepository usuarioRepository,
                         LocalizacaoService localizacaoService) {
        this.cidadeRepository = cidadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.localizacaoService = localizacaoService;
    }

    // Read do CRUD + Formulário de Filtro: lista todas ou só as que contêm o termo buscado no nome.
    @Transactional(readOnly = true)
    public List<Cidade> listarTodas(String busca) {
        if (busca != null && !busca.trim().isEmpty()) {
            return cidadeRepository.findByDescricaoContainingIgnoreCaseOrderByDescricaoAsc(busca.trim());
        }
        return cidadeRepository.findAllByOrderByDescricaoAsc();
    }

    // Quantos membros moram em cada cidade (mostrado na tabela e usado para bloquear exclusões).
    @Transactional(readOnly = true)
    public Map<Integer, Long> contarMembrosPorCidade() {
        Map<Integer, Long> contagem = new HashMap<>();
        for (Object[] linha : usuarioRepository.contarMembrosPorCidade()) {
            contagem.put((Integer) linha[0], (Long) linha[1]);
        }
        return contagem;
    }

    @Transactional(readOnly = true)
    public Cidade buscarPorId(Integer id) {
        return cidadeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cidade não encontrada para o ID: " + id));
    }

    // Create do CRUD.
    @Transactional
    public Cidade criarCidade(CidadeForm form) {
        String nome = localizacaoService.normalizarNomeCidade(form.getNome());
        UF uf = localizacaoService.obterOuCriarUf(form.getUf());
        garantirQueNaoExiste(nome, uf, null);

        Cidade cidade = new Cidade();
        cidade.setDescricao(nome);
        cidade.setUF(uf);
        return cidadeRepository.save(cidade);
    }

    // Update do CRUD. Os membros que moram na cidade passam a ver o nome novo automaticamente.
    @Transactional
    public Cidade atualizarCidade(Integer id, CidadeForm form) {
        Cidade cidade = buscarPorId(id);
        String nome = localizacaoService.normalizarNomeCidade(form.getNome());
        UF uf = localizacaoService.obterOuCriarUf(form.getUf());
        garantirQueNaoExiste(nome, uf, id);

        cidade.setDescricao(nome);
        cidade.setUF(uf);
        return cidadeRepository.save(cidade);
    }

    // Delete do CRUD. Cidade em uso por algum membro não pode ser excluída (o banco recusaria pela chave estrangeira).
    @Transactional
    public void excluirCidade(Integer id) {
        Cidade cidade = buscarPorId(id);
        long membros = usuarioRepository.countByCidade_Id(id);
        if (membros > 0) {
            throw new IllegalStateException("Não é possível excluir: " + membros
                    + (membros == 1 ? " membro mora" : " membros moram") + " nessa cidade.");
        }
        cidadeRepository.delete(cidade);
    }

    private void garantirQueNaoExiste(String nome, UF uf, Integer idAtual) {
        cidadeRepository.buscarPorNomeEUf(nome, uf)
                .filter(existente -> !existente.getId().equals(idAtual))
                .ifPresent(existente -> {
                    throw new IllegalStateException("A cidade " + existente.getDescricao() + "/" + uf.getUF() + " já está cadastrada.");
                });
    }
}
