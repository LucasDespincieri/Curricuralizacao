package com.example.curriruralizacao.service;

import com.example.curriruralizacao.DTO.NovoCapituloForm;
import com.example.curriruralizacao.DTO.NovoLivroForm;
import com.example.curriruralizacao.model.Capitulo;
import com.example.curriruralizacao.model.ENUMS.Roles;
import com.example.curriruralizacao.model.Livro;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.repository.CapituloRepository;
import com.example.curriruralizacao.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service: Camada de regras de negócio responsável pelo gerenciamento de livros e seus capítulos.
@Service
public class LivroService {

    // Dependências de acesso a dados injetadas via construtor.
    private final LivroRepository livroRepository;
    private final CapituloRepository capituloRepository;

    public LivroService(LivroRepository livroRepository, CapituloRepository capituloRepository) {
        this.livroRepository = livroRepository;
        this.capituloRepository = capituloRepository;
    }

    // Lista todos os livros cadastrados na biblioteca da igreja, ordenados da publicação mais recente para a mais antiga.
    @Transactional(readOnly = true)
    public List<Livro> listarTodosLivros() {
        return livroRepository.findAllByOrderByDataPublicacaoDesc();
    }

    // Busca um livro específico pelo seu identificador único no banco de dados.
    @Transactional(readOnly = true)
    public Livro buscarLivroPorId(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado para o ID: " + id));
    }

    // Localiza um capítulo específico pertencente a um livro com base no seu número ordinal de leitura.
    @Transactional(readOnly = true)
    public Capitulo buscarCapituloPorOrdem(Long livroId, Integer ordem) {
        return capituloRepository.findByLivroIdAndOrdem(livroId, ordem)
                .orElseThrow(() -> new IllegalArgumentException("Capítulo " + ordem + " não encontrado para o livro ID: " + livroId));
    }

    // Retorna todos os capítulos de uma determinada obra em ordem crescente (Capítulo 1, 2, 3...).
    @Transactional(readOnly = true)
    public List<Capitulo> listarCapitulosDoLivro(Long livroId) {
        return capituloRepository.findByLivroIdOrderByOrdemAsc(livroId);
    }

    // Cadastra um novo livro na plataforma com base nas informações do formulário e vincula ao Pastor autor.
    @Transactional
    public Livro criarLivro(NovoLivroForm form, Usuario pastor) {
        // Validação de segurança: apenas o perfil ADM (Pastor) pode publicar livros.
        validarPermissaoPastor(pastor);

        Livro livro = new Livro();
        livro.setTitulo(form.getTitulo().trim());
        livro.setAutor(pastor);

        return livroRepository.save(livro);
    }

    // Adiciona um novo capítulo a um livro existente após validar permissões e regras de unicidade de ordem.
    @Transactional
    public Capitulo adicionarCapitulo(Long livroId, NovoCapituloForm form, Usuario pastor) {
        // Validação de segurança para garantir privilégio administrativo.
        validarPermissaoPastor(pastor);

        Livro livro = buscarLivroPorId(livroId);

        // Regra de negócio: impede capítulos duplicados com o mesmo número de ordem na mesma obra.
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

    // Exclui um livro da biblioteca (os capítulos e grifos associados são removidos em cascata pelo JPA).
    @Transactional
    public void excluirLivro(Long livroId, Usuario pastor) {
        validarPermissaoPastor(pastor);
        Livro livro = buscarLivroPorId(livroId);

        livroRepository.delete(livro);
    }

    // Exclui um capítulo específico do livro.
    @Transactional
    public void excluirCapitulo(Long capituloId, Usuario pastor) {
        validarPermissaoPastor(pastor);
        Capitulo capitulo = capituloRepository.findById(capituloId)
                .orElseThrow(() -> new IllegalArgumentException("Capítulo não encontrado para o ID: " + capituloId));

        capituloRepository.delete(capitulo);
    }

    // Método auxiliar privado para validar se o usuário solicitante possui o papel de Administrador/Pastor.
    private void validarPermissaoPastor(Usuario usuario) {
        if (usuario == null || usuario.getRole() != Roles.ADM) {
            throw new SecurityException("Regra de Negócio (RN03): Apenas perfis com papel ADMIN (Pastor) têm permissão de criar, editar ou excluir Livros e Capítulos.");
        }
    }
}