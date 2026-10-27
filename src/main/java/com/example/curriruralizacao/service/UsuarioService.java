package com.example.curriruralizacao.service;

import com.example.curriruralizacao.model.Usuario;
import com.example.curriruralizacao.repository.LivroRepository;
import com.example.curriruralizacao.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

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

    public UsuarioService(UsuarioRepository usuarioRepository,
                          LivroRepository livroRepository) {
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
    }
}
