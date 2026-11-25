package com.example.curriruralizacao.service;

import com.example.curriruralizacao.DTO.CadastroForm;
import com.example.curriruralizacao.DTO.MembroForm;
import com.example.curriruralizacao.model.ENUMS.Roles;
import com.example.curriruralizacao.model.Livro;
import com.example.curriruralizacao.model.Post;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.repository.LivroRepository;
import com.example.curriruralizacao.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

// Regras do CRUD de Membros (Usuário), usado pela Administração do Pastor.
// Travas de segurança: o Pastor não pode se excluir nem tirar o próprio papel, e o sistema
// nunca fica sem nenhum Pastor (ADM).
@Service
public class MembroService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final LivroRepository livroRepository;
    private final LocalizacaoService localizacaoService;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    public MembroService(UsuarioRepository usuarioRepository, UsuarioService usuarioService,
                         LivroRepository livroRepository, LocalizacaoService localizacaoService,
                         CloudinaryService cloudinaryService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.livroRepository = livroRepository;
        this.localizacaoService = localizacaoService;
        this.cloudinaryService = cloudinaryService;
        this.passwordEncoder = passwordEncoder;
    }

    // Read + Formulário de Filtro: todos os membros, ou só os que têm o termo no nome ou no e-mail.
    @Transactional(readOnly = true)
    public List<Usuario> listar(String busca) {
        if (busca != null && !busca.trim().isEmpty()) {
            return usuarioRepository.buscarPorNomeOuEmail(busca.trim());
        }
        return usuarioRepository.findAllByOrderByNomeAsc();
    }

    // Create: o Pastor cadastra um membro com senha inicial (o membro pode entrar e usar o site normalmente).
    @Transactional
    public Usuario criar(MembroForm form) {
        String nome = validarNome(form.getNome());
        String email = Objects.toString(form.getEmail(), "").trim().toLowerCase();
        if (!EMAIL.matcher(email).matches() || email.length() > 150) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        String senha = Objects.toString(form.getSenha(), "");
        if (senha.length() < 6) {
            throw new IllegalArgumentException("A senha inicial deve ter no mínimo 6 caracteres.");
        }

        CadastroForm cadastro = new CadastroForm();
        cadastro.setNome(nome);
        cadastro.setEmail(email);
        cadastro.setSenha(senha);
        Usuario usuario = usuarioService.cadastrarUsuario(cadastro); // recusa e-mail repetido

        usuario.setRole(converterPapel(form.getPapel()));
        aplicarCidade(usuario, form);
        return usuarioRepository.save(usuario);
    }

    // Update: nome, papel e cidade/UF. O e-mail não muda (é o login).
    @Transactional
    public Usuario atualizar(Integer id, MembroForm form, Usuario pastorLogado) {
        Usuario usuario = usuarioService.buscarPorId(id);
        Roles novoPapel = converterPapel(form.getPapel());

        if (usuario.getId().equals(pastorLogado.getId()) && novoPapel != Roles.ADM) {
            throw new IllegalStateException("Você não pode tirar o seu próprio papel de Pastor.");
        }
        if (usuario.getRole() == Roles.ADM && novoPapel != Roles.ADM && usuarioRepository.countByRole(Roles.ADM) <= 1) {
            throw new IllegalStateException("O sistema precisa ter pelo menos um Pastor.");
        }

        usuario.setNome(validarNome(form.getNome()));
        usuario.setRole(novoPapel);
        aplicarCidade(usuario, form);
        return usuarioRepository.save(usuario);
    }

    // Só o Pastor muda senhas: define uma senha nova para qualquer conta (inclusive a dele), pela Administração.
    // A troca invalida o "lembrar de mim" daquela conta, porque o cookie é assinado com a senha antiga.
    @Transactional
    public Usuario redefinirSenha(Integer id, String novaSenha) {
        Usuario usuario = usuarioService.buscarPorId(id);
        String senha = Objects.toString(novaSenha, "");
        if (senha.length() < 6) {
            throw new IllegalArgumentException("A nova senha deve ter no mínimo 6 caracteres.");
        }
        usuario.setSenha(passwordEncoder.encode(senha));
        return usuarioRepository.save(usuario);
    }

    // Delete: apaga a conta e tudo o que é do membro (posts, comentários, reações, grifos, estante e livros).
    @Transactional
    public void excluir(Integer id, Usuario pastorLogado) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario.getId().equals(pastorLogado.getId())) {
            throw new IllegalStateException("Você não pode excluir a sua própria conta por aqui.");
        }
        if (usuario.getRole() == Roles.ADM && usuarioRepository.countByRole(Roles.ADM) <= 1) {
            throw new IllegalStateException("O sistema precisa ter pelo menos um Pastor.");
        }

        // Livros do membro: não têm cascade a partir do usuário, então saem antes (e da estante de todos).
        for (Livro livro : livroRepository.findAllByAutor_Id(id)) {
            livroRepository.removerDeTodasAsEstantes(livro.getId());
            livroRepository.delete(livro);
        }

        // Imagens dos posts do membro: apagadas do Cloudinary depois que a exclusão for gravada.
        List<String> imagens = usuario.getPosts().stream()
                .map(Post::getImagemUrl)
                .filter(Objects::nonNull)
                .toList();
        if (!imagens.isEmpty()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    imagens.forEach(cloudinaryService::excluirImagem);
                }
            });
        }

        // Posts, comentários, reações e grifos saem pelo cascade do Usuario; a estante (tabela de junção) também.
        usuarioRepository.delete(usuario);
    }

    private String validarNome(String nome) {
        String n = Objects.toString(nome, "").trim().replaceAll("\\s+", " ");
        if (n.length() < 3 || n.length() > 50) {
            throw new IllegalArgumentException("O nome deve ter entre 3 e 50 caracteres.");
        }
        return n;
    }

    private Roles converterPapel(String papel) {
        if ("PASTOR".equalsIgnoreCase(papel)) return Roles.ADM;
        if (papel == null || "MEMBRO".equalsIgnoreCase(papel)) return Roles.USR;
        throw new IllegalArgumentException("Papel inválido. Use PASTOR ou MEMBRO.");
    }

    private void aplicarCidade(Usuario usuario, MembroForm form) {
        String cidade = Objects.toString(form.getCidade(), "").trim();
        String uf = Objects.toString(form.getUf(), "").trim();
        if (cidade.isEmpty() && uf.isEmpty()) {
            usuario.setCidade(null);
        } else if (cidade.isEmpty() || uf.isEmpty()) {
            throw new IllegalArgumentException("Preencha cidade e UF juntos (ou deixe os dois em branco).");
        } else {
            usuario.setCidade(localizacaoService.obterOuCriarCidade(cidade, uf));
        }
    }
}
