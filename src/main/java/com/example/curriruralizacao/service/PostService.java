package com.example.curriruralizacao.service;

import com.example.curriruralizacao.DTO.NovoComentarioForm;
import com.example.curriruralizacao.DTO.NovoPostForm;
import com.example.curriruralizacao.model.Comentarios;
import com.example.curriruralizacao.model.ENUMS.Roles;
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

// @Service: Camada de regras de negócio responsável pelas publicações, comentários e reações no feed.
@Service
public class PostService {

    // Injeção dos repositórios de acesso a dados para posts, comentários e reações.
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

    // Retorna todas as publicações do feed ordenadas cronologicamente da mais recente para a mais antiga.
    @Transactional(readOnly = true)
    public List<Post> listarFeed() {
        return postRepository.findAllByOrderByDataCriacaoDesc();
    }

    // Busca uma postagem específica pelo seu identificador primário (ID).
    @Transactional(readOnly = true)
    public Post buscarPorId(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Postagem não encontrada para o ID: " + id));
    }

    // Cria e persiste uma nova publicação no feed vinculada ao autor autenticado.
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

    // Adiciona um novo comentário a uma publicação existente no feed.
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

    // Remove uma publicação do feed caso o usuário solicitante seja o próprio autor ou o Administrador (Pastor).
    @Transactional
    public void excluirPost(Long postId, Usuario usuarioSolicitante) {
        Post post = buscarPorId(postId);

        // Verifica se o solicitante é o autor do post ou se possui papel administrativo (Roles.ADM).
        boolean isAutor = post.getAutor().getId().equals(usuarioSolicitante.getId());
        boolean isAdmin = usuarioSolicitante.getRole() == Roles.ADM;

        // Se não for o autor nem o administrador, bloqueia a operação lançando erro de segurança.
        if (!isAutor && !isAdmin) {
            throw new SecurityException("Apenas o autor ou o Pastor administrador possuem permissão para excluir esta postagem.");
        }
        postRepository.delete(post);
    }

    // Alterna uma reação no post: se já reagiu com o mesmo tipo, remove a reação; se for tipo diferente, atualiza; se não reagiu, cria nova reação.
    @Transactional
    public void alternarReacao(Long postId, Usuario usuario, String tipo) {
        Post post = buscarPorId(postId);
        // Busca se o usuário já possui alguma reação nesta publicação.
        Optional<ReacaoPost> reacaoExistente = reacaoPostRepository.findByPostIdAndUsuarioId(postId, usuario.getId());

        if (reacaoExistente.isPresent()) {
            ReacaoPost reacao = reacaoExistente.get();
            // Se clicou no mesmo tipo de reação que já havia dado (toggle), cancela a reação removendo-a.
            if (reacao.getTipo().equalsIgnoreCase(tipo)) {
                reacaoPostRepository.delete(reacao);
            } else {
                // Caso contrário, altera o tipo da reação para a nova escolha (ex: de "CURTIR" para "AMEM").
                reacao.setTipo(tipo.toUpperCase());
                reacaoPostRepository.save(reacao);
            }
        } else {
            // Se o usuário ainda não havia reagido, registra a nova reação no banco de dados.
            ReacaoPost novaReacao = new ReacaoPost(tipo.toUpperCase(), post, usuario);
            reacaoPostRepository.save(novaReacao);
        }
    }
}
