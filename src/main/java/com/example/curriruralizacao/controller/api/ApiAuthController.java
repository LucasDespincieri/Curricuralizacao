package com.example.curriruralizacao.controller.api;

import com.example.curriruralizacao.DTO.CadastroForm;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.repository.UsuarioRepository;
import com.example.curriruralizacao.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public ApiAuthController(AuthenticationManager authenticationManager,
                             UsuarioService usuarioService,
                             UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        return ResponseEntity.ok(ApiMapper.toUsuarioDto(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String senha = body.getOrDefault("senha", "");

        try {
            authenticateSession(email, senha, request);
            Usuario user = usuarioService.buscarPorEmail(email);
            return ResponseEntity.ok(ApiMapper.toUsuarioDto(user));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "E-mail ou senha inválidos."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String nome = body.getOrDefault("nome", "").trim();
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String senha = body.getOrDefault("senha", "");

        if (nome.length() < 3) {
            return ResponseEntity.badRequest().body(Map.of("error", "O nome deve ter pelo menos 3 caracteres."));
        }
        if (email.isBlank() || !email.contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Informe um e-mail válido."));
        }
        if (senha.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "A senha deve ter no mínimo 6 caracteres."));
        }

        CadastroForm form = new CadastroForm(nome, email, senha);

        try {
            Usuario user = usuarioService.cadastrarUsuario(form);
            if (body.containsKey("bairro")) {
                user.setBairro(body.get("bairro"));
            }
            if (body.get("nascimento") != null && !body.get("nascimento").isBlank()) {
                user.setDataNascimento(LocalDate.parse(body.get("nascimento")));
            }
            user = usuarioRepository.save(user);
            authenticateSession(user.getEmail(), senha, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.toUsuarioDto(user));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    private void authenticateSession(String email, String senha, HttpServletRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, senha)
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        if (body.get("nome") != null && !body.get("nome").isBlank()) {
            user.setNome(body.get("nome").trim());
        }
        if (body.get("bairro") != null) {
            user.setBairro(body.get("bairro").trim());
        }
        user = usuarioRepository.save(user);
        return ResponseEntity.ok(ApiMapper.toUsuarioDto(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
