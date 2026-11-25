package com.example.curriruralizacao.controller;

import com.example.curriruralizacao.DTO.NovoComentarioForm;
import com.example.curriruralizacao.DTO.NovoPostForm;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.service.PostService;
import com.example.curriruralizacao.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

// @Controller: Controlador MVC responsável pela timeline/feed da comunidade, criação de posts, comentários e reações.
// @RequestMapping("/feed"): Define o prefixo base de rota para todos os métodos deste controller.
@Controller
@RequestMapping("/feed")
public class FeedController {

    // Serviços de negócio necessários para alimentar e gerenciar o feed.
    private final PostService postService;
    private final UsuarioService usuarioService;

    // Injeção de dependências via construtor.
    public FeedController(PostService postService, UsuarioService usuarioService) {
        this.postService = postService;
        this.usuarioService = usuarioService;
    }

    // Endpoint GET /feed: Carrega as publicações e formulários da página principal da rede social.
    // Principal: Fornecido pelo Spring Security com os dados do usuário atualmente autenticado na sessão.
    @GetMapping
    public String carregarFeed(Model model, Principal principal) {
        // Envia para a view a lista de todos os posts com comentários e reações em ordem decrescente de data.
        model.addAttribute("posts", postService.listarFeed());

        // Inicializa os DTOs de formulário nos atributos do modelo, caso não tenham sido preenchidos por redirecionamentos.
        if (!model.containsAttribute("novoPostForm")) {
            model.addAttribute("novoPostForm", new NovoPostForm());
        }
        if (!model.containsAttribute("novoComentarioForm")) {
            model.addAttribute("novoComentarioForm", new NovoComentarioForm());
        }

        // Se houver uma sessão autenticada ativa, disponibiliza a entidade do usuário logado para a view Thymeleaf.
        if (principal != null) {
            Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
            model.addAttribute("usuarioLogado", usuarioLogado);
        }

        // Renderiza o template localizado em src/main/resources/templates/feed/index.html.
        return "feed/index";
    }

    // Endpoint POST /feed/posts/novo: Recebe a submissão de uma nova mensagem/postagem para o feed.
    @PostMapping("/posts/novo")
    public String criarPost(@Valid @ModelAttribute("novoPostForm") NovoPostForm form,
                            BindingResult bindingResult,
                            Principal principal,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        // Se a requisição foi feita sem autenticação, redireciona para a tela de login.
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());

        // Se o texto da publicação for inválido (vazio ou exceder limite), devolve a mesma página com os erros exibidos.
        if (bindingResult.hasErrors()) {
            model.addAttribute("posts", postService.listarFeed());
            model.addAttribute("usuarioLogado", usuarioLogado);
            if (!model.containsAttribute("novoComentarioForm")) {
                model.addAttribute("novoComentarioForm", new NovoComentarioForm());
            }
            return "feed/index";
        }

        // Cria o post associado ao usuário logado e adiciona mensagem de sucesso flash (sobrevive ao redirect).
        postService.criarPost(form, usuarioLogado);
        redirectAttributes.addFlashAttribute("alertaSucesso", "Mensagem publicada com sucesso no feed da igreja!");
        return "redirect:/feed";
    }

    // Endpoint POST /feed/posts/{id}/comentar: Adiciona uma resposta a uma postagem existente.
    @PostMapping("/posts/{id}/comentar")
    public String adicionarComentario(@PathVariable("id") Long id,
                                      @Valid @ModelAttribute("novoComentarioForm") NovoComentarioForm form,
                                      BindingResult bindingResult,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        // Se o comentário estiver vazio, notifica o usuário via flash attribute e rola a tela de volta para o post.
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("alertaErroComentario", "O comentário não pode ser vazio.");
            return "redirect:/feed#post-" + id;
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
        postService.adicionarComentario(id, form, usuarioLogado);
        return "redirect:/feed#post-" + id;
    }

    // Endpoint POST /feed/posts/{id}/excluir: Permite que o autor da publicação ou o Administrador a excluam.
    @PostMapping("/posts/{id}/excluir")
    public String excluirPost(@PathVariable("id") Long id,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
        try {
            postService.excluirPost(id, usuarioLogado);
            redirectAttributes.addFlashAttribute("alertaSucesso", "Postagem excluída com sucesso.");
        } catch (SecurityException ex) {
            // Se o usuário não tiver permissão para excluir a postagem, captura a exceção e exibe aviso de segurança.
            redirectAttributes.addFlashAttribute("alertaErro", ex.getMessage());
        }

        return "redirect:/feed";
    }

    // Endpoint POST /feed/posts/{id}/reagir: Alterna a reação do usuário em um post (curtida, apoio, etc.).
    @PostMapping("/posts/{id}/reagir")
    public String alternarReacao(@PathVariable("id") Long id,
                                 @RequestParam("tipo") String tipo,
                                 Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
        postService.alternarReacao(id, usuarioLogado, tipo);
        // Retorna o foco da página diretamente para o elemento visual da postagem reagida.
        return "redirect:/feed#post-" + id;
    }
}
