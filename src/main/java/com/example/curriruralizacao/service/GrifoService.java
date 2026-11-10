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

@Service
public class GrifoService {

    private final GrifoRepository grifoRepository;
    private final CapituloRepository capituloRepository;

    public GrifoService(GrifoRepository grifoRepository, CapituloRepository capituloRepository) {
        this.grifoRepository = grifoRepository;
        this.capituloRepository = capituloRepository;
    }

    @Transactional
    public Grifo salvarGrifo(NovoGrifoForm form, Usuario usuario) {
        if (usuario == null) {
            throw new SecurityException("Apenas usuários autenticados podem salvar grifos e anotações.");
        }

        Capitulo capitulo = capituloRepository.findById(form.getCapituloId())
                .orElseThrow(() -> new IllegalArgumentException("Capítulo não encontrado para o ID: " + form.getCapituloId()));

        Grifo grifo = new Grifo();
        grifo.setTextoSelecionado(form.getTextoSelecionado().trim());
        if (form.getAnotacao() != null && !form.getAnotacao().trim().isEmpty()) {
            grifo.setAnotacao(form.getAnotacao().trim());
        }
        grifo.setCapitulo(capitulo);
        grifo.setUsuario(usuario);

        return grifoRepository.save(grifo);
    }

    @Transactional(readOnly = true)
    public List<Grifo> listarGrifosDoCapitulo(Long capituloId, Integer usuarioId) {
        if (usuarioId == null) {
            return Collections.emptyList();
        }
        return grifoRepository.findAllByCapituloIdAndUsuarioId(capituloId, usuarioId);
    }

    @Transactional
    public void excluirGrifo(Long grifoId, Usuario usuario) {
        Grifo grifo = grifoRepository.findById(grifoId)
                .orElseThrow(() -> new IllegalArgumentException("Grifo não encontrado para o ID: " + grifoId));

        if (!grifo.getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("Você não tem permissão para remover anotações de outro participante.");
        }

        grifoRepository.delete(grifo);
    }
}
