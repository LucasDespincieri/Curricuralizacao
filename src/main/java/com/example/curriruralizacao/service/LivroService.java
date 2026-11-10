package com.example.curriruralizacao.service;

import com.example.curriruralizacao.DTO.NovoCapituloForm;
import com.example.curriruralizacao.DTO.NovoLivroForm;
import com.example.curriruralizacao.model.Capitulo;
import com.example.curriruralizacao.model.Livro;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.repository.CapituloRepository;
import com.example.curriruralizacao.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final CapituloRepository capituloRepository;

    public LivroService(LivroRepository livroRepository, CapituloRepository capituloRepository) {
        this.livroRepository = livroRepository;
        this.capituloRepository = capituloRepository;
    }

    @Transactional(readOnly = true)
    public List<Livro> listarTodosLivros() {
        return livroRepository.findAllByOrderByDataPublicacaoDesc();
    }

    @Transactional(readOnly = true)
    public Livro buscarLivroPorId(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado para o ID: " + id));
    }

    @Transactional(readOnly = true)
    public Capitulo buscarCapituloPorOrdem(Long livroId, Integer ordem) {
        return capituloRepository.findByLivroIdAndOrdem(livroId, ordem)
                .orElseThrow(() -> new IllegalArgumentException("Capítulo " + ordem + " não encontrado para o livro ID: " + livroId));
    }

    @Transactional(readOnly = true)
    public List<Capitulo> listarCapitulosDoLivro(Long livroId) {
        return capituloRepository.findByLivroIdOrderByOrdemAsc(livroId);
    }

    @Transactional
    public Livro criarLivro(NovoLivroForm form, Usuario pastor) {
        validarPermissaoPastor(pastor);

        Livro livro = new Livro();
        livro.setTitulo(form.getTitulo().trim());
        livro.setAutor(pastor);

        return livroRepository.save(livro);
    }

    @Transactional
    public Capitulo adicionarCapitulo(Long livroId, NovoCapituloForm form, Usuario pastor) {
        validarPermissaoPastor(pastor);

        Livro livro = buscarLivroPorId(livroId);

        if (capituloRepository.existsByLivroIdAndOrdem(livroId, form.getOrdem())) {
            throw new IllegalArgumentException("Já existe um capítulo com o número de ordem " + form.getOrdem() + " cadastrado para este livro.");
        }

        Capitulo capitulo = new Capitulo();
        capitulo.setLivro(livro);
        capitulo.setOrdem(form.getOrdem());
        capitulo.setTitulo(form.getTitulo().trim());
        capitulo.setConteudo(form.getConteudo().trim());

        return capituloRepository.save(capitulo);
    }

    @Transactional
    public void excluirLivro(Long livroId, Usuario pastor) {
        validarPermissaoPastor(pastor);
        Livro livro = buscarLivroPorId(livroId);

        livroRepository.delete(livro);
    }

    @Transactional
    public void excluirCapitulo(Long capituloId, Usuario pastor) {
        validarPermissaoPastor(pastor);
        Capitulo capitulo = capituloRepository.findById(capituloId)
                .orElseThrow(() -> new IllegalArgumentException("Capítulo não encontrado para o ID: " + capituloId));

        capituloRepository.delete(capitulo);
    }

    private void validarPermissaoPastor(Usuario usuario) {
        if (usuario == null || !"ROLE_ADMIN".equals(usuario.getRole())) {
            throw new SecurityException("Regra de Negócio (RN03): Apenas perfis com papel ADMIN (Pastor) têm permissão de criar, editar ou excluir Livros e Capítulos.");
        }
    }
}