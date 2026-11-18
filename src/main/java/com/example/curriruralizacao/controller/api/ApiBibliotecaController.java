package com.example.curriruralizacao.controller.api;

import com.example.curriruralizacao.DTO.NovoGrifoForm;
import com.example.curriruralizacao.model.Capitulo;
import com.example.curriruralizacao.model.Grifo;
import com.example.curriruralizacao.model.Livro;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.repository.CapituloRepository;
import com.example.curriruralizacao.repository.LivroRepository;
import com.example.curriruralizacao.service.GrifoService;
import com.example.curriruralizacao.service.LivroService;
import com.example.curriruralizacao.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiBibliotecaController {

    private final LivroService livroService;
    private final LivroRepository livroRepository;
    private final CapituloRepository capituloRepository;
    private final GrifoService grifoService;
    private final UsuarioService usuarioService;

    public ApiBibliotecaController(LivroService livroService,
                                   LivroRepository livroRepository,
                                   CapituloRepository capituloRepository,
                                   GrifoService grifoService,
                                   UsuarioService usuarioService) {
        this.livroService = livroService;
        this.livroRepository = livroRepository;
        this.capituloRepository = capituloRepository;
        this.grifoService = grifoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/livros")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarLivros() {
        return livroService.listarTodosLivros().stream()
                .map(l -> ApiMapper.toLivroDto(l, livroService.listarCapitulosDoLivro(l.getId())))
                .toList();
    }

    @GetMapping("/livros/{id}")
    @Transactional(readOnly = true)
    public Map<String, Object> buscarLivro(@PathVariable Long id) {
        Livro livro = livroService.buscarLivroPorId(id);
        return ApiMapper.toLivroDto(livro, livroService.listarCapitulosDoLivro(id));
    }

    @PostMapping("/livros")
    @Transactional
    public ResponseEntity<?> criarLivro(@RequestBody Map<String, Object> body, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        String titulo = Objects.toString(body.get("titulo"), "").trim();
        if (titulo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Título é obrigatório"));
        }

        Livro livro = new Livro(titulo.length() > 200 ? titulo.substring(0, 200) : titulo, user);
        livro = livroRepository.save(livro);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> secoes = (List<Map<String, Object>>) body.getOrDefault("conteudoSecoes", List.of());
        List<Capitulo> salvos = new ArrayList<>();
        int ordem = 1;
        for (Map<String, Object> s : secoes) {
            String tituloSecao = Objects.toString(s.get("tituloSecao"), "Seção " + ordem).trim();
            String texto = Objects.toString(s.get("texto"), "").trim();
            Capitulo cap = new Capitulo(ordem++, tituloSecao, texto, livro);
            salvos.add(capituloRepository.save(cap));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.toLivroDto(livro, salvos));
    }

    @GetMapping("/estante")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listarEstante(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        List<Map<String, Object>> items = user.getLivrosFavoritos().stream().map(livro -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idBiblioteca", livro.getId());
            item.put("idUsuario", user.getId());
            item.put("idLivros", livro.getId());
            item.put("livro", ApiMapper.toLivroDto(livro, livroService.listarCapitulosDoLivro(livro.getId())));
            return item;
        }).toList();
        return ResponseEntity.ok(items);
    }

    @PostMapping("/estante/{livroId}")
    @Transactional
    public ResponseEntity<?> adicionarEstante(@PathVariable Long livroId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        usuarioService.adicionarLivroABiblioteca(livroId, user.getId());
        return ResponseEntity.ok(Map.of("saved", true));
    }

    @DeleteMapping("/estante/{livroId}")
    @Transactional
    public ResponseEntity<?> removerEstante(@PathVariable Long livroId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        usuarioService.removerLivroDaBiblioteca(livroId, user.getId());
        return ResponseEntity.ok(Map.of("saved", false));
    }

    @GetMapping("/livros/{livroId}/capitulos/{ordem}/grifos")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listarGrifos(@PathVariable Long livroId,
                                          @PathVariable Integer ordem,
                                          Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(List.of());
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        Capitulo cap = livroService.buscarCapituloPorOrdem(livroId, ordem);
        List<Map<String, Object>> list = grifoService.listarGrifosDoCapitulo(cap.getId(), user.getId())
                .stream()
                .map(g -> ApiMapper.toGrifoDto(g, livroId, ordem))
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/livros/{livroId}/capitulos/{ordem}/grifos")
    @Transactional
    public ResponseEntity<?> criarGrifo(@PathVariable Long livroId,
                                        @PathVariable Integer ordem,
                                        @RequestBody Map<String, String> body,
                                        Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        Capitulo cap = livroService.buscarCapituloPorOrdem(livroId, ordem);
        NovoGrifoForm form = new NovoGrifoForm(cap.getId(), body.getOrDefault("trecho", ""), body.get("anotacao"));
        Grifo saved = grifoService.salvarGrifo(form, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.toGrifoDto(saved, livroId, ordem));
    }

    @DeleteMapping("/grifos/{grifoId}")
    @Transactional
    public ResponseEntity<?> excluirGrifo(@PathVariable Long grifoId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        }
        Usuario user = usuarioService.buscarPorEmail(principal.getName());
        grifoService.excluirGrifo(grifoId, user);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
