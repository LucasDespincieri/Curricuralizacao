package com.example.curriruralizacao.controller.api;

import com.example.curriruralizacao.DTO.MembroForm;
import com.example.curriruralizacao.model.ENUMS.Roles;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.service.MembroService;
import com.example.curriruralizacao.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// @RestController: CRUD de Membros (Usuário) da área de Administração. Só o Pastor (ADM) acessa.
//   GET    /api/membros?busca=...  → lista (filtro por nome ou e-mail)
//   POST   /api/membros            → cadastra { nome, email, senha, papel, cidade, uf }
//   PUT    /api/membros/{id}       → edita    { nome, papel, cidade, uf }
//   DELETE /api/membros/{id}       → exclui a conta e tudo o que é do membro
@RestController
@RequestMapping("/api/membros")
public class ApiMembroController {

    private final MembroService membroService;
    private final UsuarioService usuarioService;

    public ApiMembroController(MembroService membroService, UsuarioService usuarioService) {
        this.membroService = membroService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> listar(@RequestParam(value = "busca", required = false) String busca, Principal principal) {
        Usuario pastor = pastorLogado(principal);
        if (pastor == null) return negado(principal);
        List<Map<String, Object>> lista = membroService.listar(busca).stream().map(ApiMembroController::toDto).toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody MembroForm form, Principal principal) {
        Usuario pastor = pastorLogado(principal);
        if (pastor == null) return negado(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(membroService.criar(form)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @RequestBody MembroForm form, Principal principal) {
        Usuario pastor = pastorLogado(principal);
        if (pastor == null) return negado(principal);
        return ResponseEntity.ok(toDto(membroService.atualizar(id, form, pastor)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Integer id, Principal principal) {
        Usuario pastor = pastorLogado(principal);
        if (pastor == null) return negado(principal);
        membroService.excluir(id, pastor); // IllegalStateException (ex.: excluir a si mesmo) vira HTTP 409
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    private static Map<String, Object> toDto(Usuario u) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", u.getId());
        dto.put("nome", u.getNome());
        dto.put("email", u.getEmail());
        dto.put("papel", ApiMapper.mapRole(u.getRole()));
        dto.put("cidade", u.getCidade() != null ? u.getCidade().getDescricao() : "");
        dto.put("uf", u.getCidade() != null && u.getCidade().getUF() != null ? u.getCidade().getUF().getUF() : "");
        dto.put("membroDesde", u.getDataCadastro() != null ? u.getDataCadastro().toLocalDate().toString() : "");
        return dto;
    }

    /** O usuário logado, se for o Pastor; null caso contrário. */
    private Usuario pastorLogado(Principal principal) {
        if (principal == null) return null;
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        return user.getRole() == Roles.ADM ? user : null;
    }

    private static ResponseEntity<?> negado(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Somente o Pastor pode gerenciar membros."));
    }
}
