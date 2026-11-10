package com.example.curriruralizacao.service;

import com.example.curriruralizacao.DTO.NovoComentarioForm;
import com.example.curriruralizacao.DTO.NovoPostForm;
import com.example.curriruralizacao.model.Comentarios;
import com.example.curriruralizacao.model.Post;
import com.example.curriruralizacao.model.ReacaoPost;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.repository.ComentarioRepository;
import com.example.curriruralizacao.repository.PostRepository;
import com.example.curriruralizacao.repository.ReacaoPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final ComentarioRepository comentarioRepository;
    private final ReacaoPostRepository reacaoPostRepository;

    public PostService(PostRepository postRepository,
                       ComentarioRepository comentarioRepository,
                       ReacaoPostRepository reacaoPostRepository) {
        this.postRepository = postRepository;
        this.comentarioRepository = comentarioRepository;
        this.reacaoPostRepository = reacaoPostRepository;
    }

    @Transactional(readOnly = true)
    public List<Post> listarFeed() {
        return postRepository.findAllByOrderByDataCriacaoDesc();
    }

    @Transactional(readOnly = true)
    public Post buscarPorId(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Postagem não encontrada para o ID: " + id));
    }

    @Transactional
    public Post criarPost(NovoPostForm form, Usuario autor) {
        if (autor == null) {
            throw new IllegalArgumentException("O autor da postagem é obrigatório.");
        }

        Post post = new Post();
        post.setConteudo(form.getConteudo().trim());
        post.setAutor(autor);

        return postRepository.save(post);
    }

    @Transactional
    public Comentarios adicionarComentario(Long postId, NovoComentarioForm form, Usuario autor) {
        if (autor == null) {
            throw new IllegalArgumentException("O autor do comentário é obrigatório.");
        }

        Post post = buscarPorId(postId);

        Comentarios comentario = new Comentarios();
        comentario.setConteudo(form.getConteudo().trim());
        comentario.setPost(post);
        comentario.setAutor(autor);

        return comentarioRepository.save(comentario);
    }

    @Transactional
    public void excluirPost(Long postId, Usuario usuarioSolicitante) {
        Post post = buscarPorId(postId);

        boolean isAutor = post.getAutor().getId().equals(usuarioSolicitante.getId());
        boolean isAdmin = "ROLE_ADMIN".equals(usuarioSolicitante.getRole());

        if (!isAutor && !isAdmin) {
            throw new SecurityException("Apenas o autor ou o Pastor administrador possuem permissão para excluir esta postagem.");
        }
        postRepository.delete(post);
    }

    @Transactional
    public void alternarReacao(Long postId, Usuario usuario, String tipo) {
        Post post = buscarPorId(postId);
        Optional<ReacaoPost> reacaoExistente = reacaoPostRepository.findByPostIdAndUsuarioId(postId, usuario.getId());

        if (reacaoExistente.isPresent()) {
            ReacaoPost reacao = reacaoExistente.get();
            if (reacao.getTipo().equalsIgnoreCase(tipo)) {
                reacaoPostRepository.delete(reacao);
            } else {
                reacao.setTipo(tipo.toUpperCase());
                reacaoPostRepository.save(reacao);
            }
        } else {
            ReacaoPost novaReacao = new ReacaoPost(tipo.toUpperCase(), post, usuario);
            reacaoPostRepository.save(novaReacao);
        }
    }
}
