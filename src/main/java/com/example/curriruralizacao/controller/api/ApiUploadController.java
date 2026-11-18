package com.example.curriruralizacao.controller.api;

import com.example.curriruralizacao.service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

    public ApiUploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
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
}
