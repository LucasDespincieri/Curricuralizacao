package com.example.curriruralizacao.service;

import com.example.curriruralizacao.DTO.CadastroForm;
import com.example.curriruralizacao.model.ENUMS.Roles;
import com.example.curriruralizacao.model.Livro;
import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.repository.LivroRepository;
import com.example.curriruralizacao.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

// @Service: Marca a classe como um serviço gerenciado pelo container do Spring.
// UserDetailsService: Interface central do Spring Security responsável por carregar os dados do usuário durante a autenticação.
// (Nota arquitetural: Esta classe atua como serviço de autenticação do Spring Security).
@Service
public class UsuarioService implements UserDetailsService {

    // Método obrigatório da interface UserDetailsService invocado automaticamente pelo Spring Security no momento do login.
    // Recebe o username (neste caso, o e-mail ou nome do usuário) informado no formulário de login.
    // Lança UsernameNotFoundException caso o usuário não seja localizado no banco de dados.
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email));

        return new User(
                usuario.getEmail(),
                usuario.getSenha(),
                Collections.singletonList(new SimpleGrantedAuthority(usuario.getRole().name()))
        );
    }

    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          LivroRepository livroRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @Transactional
    public Usuario cadastrarUsuario(CadastroForm form) {
        if (usuarioRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("O e-mail '" + form.getEmail() + "' já está em uso por outro membro.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(form.getNome().trim());
        usuario.setEmail(form.getEmail().trim().toLowerCase());
        usuario.setSenha(passwordEncoder.encode(form.getSenha()));
        usuario.setRole(Roles.USR);

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado para o ID: " + id));
    }

    @Transactional
    public void adicionarLivroABiblioteca(Long livroId, Integer usuarioId) {
        Usuario usuario = buscarPorId(usuarioId);
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado para o ID: " + livroId));

        usuario.getLivrosFavoritos().add(livro);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void removerLivroDaBiblioteca(Long livroId, Integer usuarioId) {
        Usuario usuario = buscarPorId(usuarioId);
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado para o ID: " + livroId));

        usuario.getLivrosFavoritos().remove(livro);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Set<Livro> listarLivrosFavoritos(Integer usuarioId) {
        Usuario usuario = buscarPorId(usuarioId);
        return usuario.getLivrosFavoritos();
    }
}
