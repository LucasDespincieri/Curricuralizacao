package com.example.curriruralizacao.service;

import com.example.curriruralizacao.DTO.NovoGrifoForm;
import com.example.curriruralizacao.model.Capitulo;
import com.example.curriruralizacao.model.Grifo;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.repository.CapituloRepository;
import com.example.curriruralizacao.repository.GrifoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

// @Service: Camada de regras de negócio responsável pelo gerenciamento de anotações e marcações (grifos) nos livros.
@Service
public class GrifoService {

    // Repositórios de dados injetados via construtor.
    private final GrifoRepository grifoRepository;
    private final CapituloRepository capituloRepository;

    public GrifoService(GrifoRepository grifoRepository, CapituloRepository capituloRepository) {
        this.grifoRepository = grifoRepository;
        this.capituloRepository = capituloRepository;
    }

    // Salva uma nova marcação de texto e anotação pessoal associada a um capítulo lido pelo usuário.
    @Transactional
    public Grifo salvarGrifo(NovoGrifoForm form, Usuario usuario) {
        // Exige que o leitor esteja autenticado para vincular a anotação ao seu perfil.
        if (usuario == null) {
            throw new SecurityException("Apenas usuários autenticados podem salvar grifos e anotações.");
        }

        // Recupera o capítulo a partir do ID informado no formulário.
        Capitulo capitulo = capituloRepository.findById(form.getCapituloId())
                .orElseThrow(() -> new IllegalArgumentException("Capítulo não encontrado para o ID: " + form.getCapituloId()));

        // Cria a entidade Grifo preenchendo o trecho selecionado, anotação e as relações.
        Grifo grifo = new Grifo();
        grifo.setTextoSelecionado(form.getTextoSelecionado().trim());
        if (form.getAnotacao() != null && !form.getAnotacao().trim().isEmpty()) {
            grifo.setAnotacao(form.getAnotacao().trim());
        }
        grifo.setCapitulo(capitulo);
        grifo.setUsuario(usuario);

        return grifoRepository.save(grifo);
    }

    // Lista todas as marcações particulares feitas pelo usuário no capítulo específico que está sendo lido.
    @Transactional(readOnly = true)
    public List<Grifo> listarGrifosDoCapitulo(Long capituloId, Integer usuarioId) {
        // Se não houver usuário logado (visitante público), retorna lista vazia.
        if (usuarioId == null) {
            return Collections.emptyList();
        }
        return grifoRepository.findAllByCapituloIdAndUsuarioId(capituloId, usuarioId);
    }

    // Exclui um grifo previamente salvo, garantindo que apenas o próprio criador possa removê-lo.
    @Transactional
    public void excluirGrifo(Long grifoId, Usuario usuario) {
        Grifo grifo = grifoRepository.findById(grifoId)
                .orElseThrow(() -> new IllegalArgumentException("Grifo não encontrado para o ID: " + grifoId));

        // Validação de privacidade: impede que outro usuário delete reflexões que não sejam suas.
        if (!grifo.getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("Você não tem permissão para remover anotações de outro participante.");
        }

        grifoRepository.delete(grifo);
    }
}
