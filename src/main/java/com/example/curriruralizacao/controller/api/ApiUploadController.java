package com.example.curriruralizacao.controller.api;

import com.example.curriruralizacao.repository.PostRepository;
import com.example.curriruralizacao.service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

// @RestController: Endpoints REST relacionados a upload de arquivos.
// Fluxo da imagem de um post:
//   1. O Angular chama POST /api/uploads/assinatura (usuário logado).
//   2. Com a assinatura, o Angular envia o arquivo direto para o Cloudinary e recebe a URL da imagem.
//   3. O Angular cria o post em POST /api/posts mandando essa URL no campo "imagemUrl".
@RestController
@RequestMapping("/api/uploads")
public class ApiUploadController {

    private final CloudinaryService cloudinaryService;
    private final PostRepository postRepository;

    public ApiUploadController(CloudinaryService cloudinaryService, PostRepository postRepository) {
        this.cloudinaryService = cloudinaryService;
        this.postRepository = postRepository;
    }

    @PostMapping("/assinatura")
    public ResponseEntity<?> gerarAssinatura(Principal principal) {
        // Só membros logados podem enviar imagens (o SecurityConfig já exige login para esta rota).
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        if (!cloudinaryService.estaConfigurado()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Envio de imagens ainda não foi configurado no servidor."));
        }
        return ResponseEntity.ok(cloudinaryService.gerarAssinaturaUpload());
    }

    // Apaga uma imagem que foi enviada mas descartada antes de publicar (o X na pré-visualização).
    // Recusa se a imagem já estiver em algum post, para ninguém apagar a foto do post de outra pessoa.
    @DeleteMapping("/imagem")
    public ResponseEntity<?> descartarImagem(@RequestBody Map<String, String> body, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        String url = body.getOrDefault("url", "");
        if (!cloudinaryService.urlValida(url)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Imagem inválida"));
        }
        if (postRepository.existsByImagemUrl(url)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Imagem em uso por um post"));
        }
        cloudinaryService.excluirImagem(url);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
