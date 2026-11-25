package com.example.curriruralizacao.controller.api;

import com.example.curriruralizacao.DTO.NovoComentarioForm;
import com.example.curriruralizacao.DTO.NovoPostForm;
import com.example.curriruralizacao.model.Comentarios;
import com.example.curriruralizacao.model.Post;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.service.CloudinaryService;
import com.example.curriruralizacao.service.PostService;
import com.example.curriruralizacao.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class ApiFeedController {

    private final PostService postService;
    private final UsuarioService usuarioService;
    private final CloudinaryService cloudinaryService;

    public ApiFeedController(PostService postService, UsuarioService usuarioService,
                             CloudinaryService cloudinaryService) {
        this.postService = postService;
        this.usuarioService = usuarioService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPosts(@RequestParam(value = "busca", required = false) String busca,
                                                 Principal principal) {
        Integer userId = principal != null ? usuarioService.buscarPorEmail(principal.getName()).getId() : null;
        // Formulário de Filtro: com ?busca=..., só os posts cujo texto contém o termo.
        return postService.listarFeedFiltrado(busca).stream()
                .map(p -> ApiMapper.toPostDto(p, userId))
                .toList();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> criarPost(@RequestBody Map<String, String> body, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        String conteudo = body.getOrDefault("conteudo", "").trim();
        if (conteudo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Conteúdo é obrigatório"));
        }

        // Imagem opcional: precisa ser uma URL do nosso Cloudinary (gerada pelo fluxo de /api/uploads/assinatura).
        String imagemUrl = body.getOrDefault("imagemUrl", "");
        imagemUrl = imagemUrl == null ? "" : imagemUrl.trim();
        if (!imagemUrl.isEmpty() && !cloudinaryService.urlValida(imagemUrl)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Imagem inválida"));
        }

        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        NovoPostForm form = new NovoPostForm();
        form.setConteudo(conteudo);
        Post post = postService.criarPost(form, user);
        if (!imagemUrl.isEmpty()) {
            post.setImagemUrl(imagemUrl);
        }
        if (body.get("titulo") != null && !body.get("titulo").isBlank()) {
            String titulo = body.get("titulo").trim();
            post.setTitulo(titulo.length() > 20 ? titulo.substring(0, 20) : titulo);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.toPostDto(post, user.getId()));
    }

    @GetMapping("/{id}/comentarios")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarComentarios(@PathVariable Long id) {
        Post post = postService.buscarPorId(id);
        return post.getComentarios().stream()
                .map(ApiMapper::toComentarioDto)
                .toList();
    }

    @PostMapping("/{id}/comentar")
    @Transactional
    public ResponseEntity<?> comentar(@PathVariable Long id,
                                      @RequestBody Map<String, String> body,
                                      Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        String conteudo = body.getOrDefault("conteudo", "").trim();
        if (conteudo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Comentário não pode ser vazio"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        NovoComentarioForm form = new NovoComentarioForm();
        form.setConteudo(conteudo);
        Comentarios comentario = postService.adicionarComentario(id, form, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.toComentarioDto(comentario));
    }

    @PostMapping("/{id}/reagir")
    @Transactional
    public ResponseEntity<?> reagir(@PathVariable Long id,
                                    @RequestBody Map<String, String> body,
                                    Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        // Reações aceitas. "ORANDO" usa a mesma coluna de texto (tipo) das outras: não muda o banco.
        String tipo = body.getOrDefault("tipo", "CURTIR").trim().toUpperCase();
        if (!java.util.List.of("CURTIR", "AMEM", "ORANDO").contains(tipo)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tipo de reação inválido"));
        }
        postService.alternarReacao(id, user, tipo);
        Post updated = postService.buscarPorId(id);
        return ResponseEntity.ok(ApiMapper.toPostDto(updated, user.getId()));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        String imagemUrl = postService.buscarPorId(id).getImagemUrl();
        postService.excluirPost(id, user);
        // Só chega aqui se o usuário tinha permissão (excluirPost lança exceção caso contrário).
        // A imagem é apagada no Cloudinary depois que a exclusão do post for gravada no banco.
        if (imagemUrl != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cloudinaryService.excluirImagem(imagemUrl);
                }
            });
        }
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
