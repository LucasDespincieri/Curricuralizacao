package com.example.curriruralizacao.service;

import com.example.curriruralizacao.model.Cidade;
import com.example.curriruralizacao.model.Pais;
import com.example.curriruralizacao.model.UF;
import com.example.curriruralizacao.repository.CidadeRepository;
import com.example.curriruralizacao.repository.PaisRepository;
import com.example.curriruralizacao.repository.UFRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Regras de localização usadas pelo perfil do membro e pelo cadastro de cidades da Administração.
// As tabelas pais e uf são preenchidas sob demanda: a UF só é criada quando alguém usa uma cidade dela.
@Service
public class LocalizacaoService {

    private static final String PAIS_PADRAO = "Brasil";

    // As 27 unidades federativas do Brasil: só elas são aceitas no cadastro.
    public static final List<String> UFS_BRASIL = List.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private final PaisRepository paisRepository;
    private final UFRepository ufRepository;
    private final CidadeRepository cidadeRepository;

    public LocalizacaoService(PaisRepository paisRepository, UFRepository ufRepository,
                              CidadeRepository cidadeRepository) {
        this.paisRepository = paisRepository;
        this.ufRepository = ufRepository;
        this.cidadeRepository = cidadeRepository;
    }

    /** Valida e padroniza o nome da cidade (sem espaços sobrando, 2 a 50 caracteres). */
    public String normalizarNomeCidade(String nomeCidade) {
        String nome = nomeCidade == null ? "" : nomeCidade.trim().replaceAll("\\s+", " ");
        if (nome.length() < 2 || nome.length() > 50) {
            throw new IllegalArgumentException("A cidade deve ter entre 2 e 50 caracteres.");
        }
        return nome;
    }

    /** Valida a sigla e devolve a UF do banco, criando-a se for a primeira vez que é usada. */
    @Transactional
    public UF obterOuCriarUf(String siglaUf) {
        String sigla = siglaUf == null ? "" : siglaUf.trim().toUpperCase();
        if (!UFS_BRASIL.contains(sigla)) {
            throw new IllegalArgumentException("UF inválida. Use a sigla de um estado brasileiro (ex.: SP).");
        }
        Pais pais = paisRepository.buscarPorDescricao(PAIS_PADRAO).orElseGet(() -> {
            Pais novo = new Pais();
            novo.setDescricao(PAIS_PADRAO);
            return paisRepository.save(novo);
        });
        return ufRepository.buscarPorSigla(sigla, pais).orElseGet(() -> {
            UF nova = new UF();
            nova.setUF(sigla);
            nova.setPais(pais);
            return ufRepository.save(nova);
        });
    }

    /** Usado pelo perfil: encontra a cidade informada pelo membro ou cadastra se ainda não existir. */
    @Transactional
    public Cidade obterOuCriarCidade(String nomeCidade, String siglaUf) {
        String nome = normalizarNomeCidade(nomeCidade);
        UF uf = obterOuCriarUf(siglaUf);
        return cidadeRepository.buscarPorNomeEUf(nome, uf).orElseGet(() -> {
            Cidade nova = new Cidade();
            nova.setDescricao(nome);
            nova.setUF(uf);
            return cidadeRepository.save(nova);
        });
    }
}
