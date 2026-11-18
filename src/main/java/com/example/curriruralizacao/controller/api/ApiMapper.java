package com.example.curriruralizacao.controller.api;

import com.example.curriruralizacao.model.*;
import com.example.curriruralizacao.model.ENUMS.Roles;

import java.util.*;

public final class ApiMapper {

    private ApiMapper() {}

    public static String mapRole(Roles role) {
        return role == Roles.ADM ? "PASTOR" : "MEMBRO";
    }

    public static Map<String, Object> toUsuarioDto(Usuario u) {
        Map<String, Object> loc = new LinkedHashMap<>();
        // Campos não informados no cadastro vão vazios (antes iam valores de exemplo, como "São Paulo").
        loc.put("idCidade", u.getCidade() != null ? u.getCidade().getId() : 0);
        loc.put("cidade", u.getCidade() != null ? u.getCidade().getDescricao() : "");
        loc.put("uf", u.getCidade() != null && u.getCidade().getUF() != null ? u.getCidade().getUF().getUF() : "");
        loc.put("pais", "Brasil");
        loc.put("bairro", u.getBairro() != null ? u.getBairro() : "");

        String username = (u.getNome() != null ? u.getNome() : "membro")
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("idUsuario", u.getId());
        dto.put("nome", u.getNome());
        dto.put("username", username.isEmpty() ? "membro" : username);
        dto.put("email", u.getEmail());
        dto.put("nascimento", u.getDataNascimento() != null ? u.getDataNascimento().toString() : "");
        dto.put("dia_cadastro", u.getDataCadastro() != null ? u.getDataCadastro().toLocalDate().toString() : "");
        dto.put("role", mapRole(u.getRole()));
        dto.put("localizacao", loc);
        return dto;
    }

    public static Map<String, Object> toPostDto(Post post, Integer currentUserId) {
        long curtir = post.getReacoes().stream().filter(r -> "CURTIR".equalsIgnoreCase(r.getTipo())).count();
        long amem = post.getReacoes().stream().filter(r -> "AMEM".equalsIgnoreCase(r.getTipo())).count();
        String userReacted = null;
        if (currentUserId != null) {
            userReacted = post.getReacoes().stream()
                    .filter(r -> r.getUsuario() != null && currentUserId.equals(r.getUsuario().getId()))
                    .map(ReacaoPost::getTipo)
                    .findFirst()
                    .orElse(null);
        }

        Map<String, Object> reacoes = new LinkedHashMap<>();
        reacoes.put("curtir", curtir);
        reacoes.put("amem", amem);
        reacoes.put("userReacted", userReacted);

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("idPost", post.getId());
        dto.put("idUsuario", post.getAutor().getId());
        dto.put("autorNome", post.getAutor().getNome());
        dto.put("autorRole", mapRole(post.getAutor().getRole()));
        dto.put("titulo", post.getTitulo());
        dto.put("data", post.getDataCriacao() != null ? post.getDataCriacao().toString() : "");
        dto.put("conteudo", post.getConteudo());
        dto.put("imagemUrl", post.getImagemUrl());
        dto.put("reacoes", reacoes);
        dto.put("comentariosCount", post.getComentarios() != null ? post.getComentarios().size() : 0);
        return dto;
    }

    public static Map<String, Object> toComentarioDto(Comentarios c) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("idComentarios", c.getId());
        dto.put("idUsuario", c.getAutor().getId());
        dto.put("autorNome", c.getAutor().getNome());
        dto.put("autorRole", mapRole(c.getAutor().getRole()));
        dto.put("idPost", c.getPost().getId());
        dto.put("data", c.getDataCriacao() != null ? c.getDataCriacao().toString() : "");
        dto.put("conteudo", c.getConteudo());
        dto.put("reacoesCount", 0);
        return dto;
    }

    public static Map<String, Object> toLivroDto(Livro livro, List<Capitulo> capitulos) {
        List<Map<String, Object>> secoes = new ArrayList<>();
        if (capitulos != null) {
            for (Capitulo cap : capitulos) {
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("id", cap.getId());
                s.put("ordem", cap.getOrdem());
                s.put("tituloSecao", cap.getTitulo());
                s.put("texto", cap.getConteudo());
                secoes.add(s);
            }
        }

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("idLivros", livro.getId());
        dto.put("idUsuario", livro.getAutor().getId());
        dto.put("autorNome", livro.getAutor().getNome());
        dto.put("autorRole", mapRole(livro.getAutor().getRole()));
        dto.put("titulo", livro.getTitulo());
        dto.put("publicado", true);
        dto.put("secoes", secoes.size());
        dto.put("conteudoSecoes", secoes);
        dto.put("data_inicio", livro.getDataPublicacao() != null ? livro.getDataPublicacao().toLocalDate().toString() : "");
        return dto;
    }

    public static Map<String, Object> toGrifoDto(Grifo g, Long livroId, Integer ordemCapitulo) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", g.getId());
        dto.put("idLivro", livroId);
        dto.put("ordemCapitulo", ordemCapitulo);
        dto.put("idUsuario", g.getUsuario().getId());
        dto.put("trecho", g.getTextoSelecionado());
        dto.put("anotacao", g.getAnotacao());
        dto.put("criadoEm", "");
        return dto;
    }
}
