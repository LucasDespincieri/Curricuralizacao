package com.example.curriruralizacao.controller.api;

import com.example.curriruralizacao.DTO.CadastroForm;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.repository.UsuarioRepository;
import com.example.curriruralizacao.service.LocalizacaoService;
import com.example.curriruralizacao.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
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

    private final LocalizacaoService localizacaoService;
    private final TokenBasedRememberMeServices rememberMeServices;

    public ApiAuthController(AuthenticationManager authenticationManager,
                             UsuarioService usuarioService,
                             UsuarioRepository usuarioRepository,
                             LocalizacaoService localizacaoService,
                             TokenBasedRememberMeServices rememberMeServices) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.localizacaoService = localizacaoService;
        this.rememberMeServices = rememberMeServices;
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
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request, HttpServletResponse response) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String senha = body.getOrDefault("senha", "");

        try {
            authenticateSession(email, senha, request, response);
            Usuario user = usuarioService.buscarPorEmail(email);
            return ResponseEntity.ok(ApiMapper.toUsuarioDto(user));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "E-mail ou senha inválidos."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body, HttpServletRequest request, HttpServletResponse response) {
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
            authenticateSession(user.getEmail(), senha, request, response);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.toUsuarioDto(user));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    private void authenticateSession(String email, String senha, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, senha)
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        // Grava o cookie "lembrar de mim": mantém o login mesmo se o servidor reiniciar.
        rememberMeServices.loginSuccess(request, response, auth);
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
        // O e-mail é o login, por isso não pode ser alterado aqui (o campo "email" é ignorado de propósito).
        String cidade = body.get("cidade");
        String uf = body.get("uf");
        if (cidade != null && uf != null) {
            if (cidade.isBlank() && uf.isBlank()) {
                user.setCidade(null);
            } else {
                try {
                    user.setCidade(localizacaoService.obterOuCriarCidade(cidade, uf));
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
                }
            }
        }
        user = usuarioRepository.save(user);
        return ResponseEntity.ok(ApiMapper.toUsuarioDto(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Apaga o cookie "lembrar de mim" antes de encerrar a sessão.
        rememberMeServices.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
