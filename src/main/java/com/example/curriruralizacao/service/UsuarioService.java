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

// @Service: Marca esta classe como um componente da camada de serviço do ecossistema Spring.
// UserDetailsService: Interface nativa do Spring Security para integração com o processo de login e autenticação.
@Service
public class UsuarioService implements UserDetailsService {

    // Declaração das dependências necessárias para as operações de usuário e segurança.
    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final PasswordEncoder passwordEncoder;

    // Injeção de dependências via construtor (prática recomendada para imutabilidade e testes).
    public UsuarioService(UsuarioRepository usuarioRepository,
                          LivroRepository livroRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método obrigatório da interface UserDetailsService chamado pelo Spring Security ao tentar fazer login.
    // @Transactional(readOnly = true): Otimiza a transação como somente leitura para consulta rápida.
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca o membro no banco de dados pelo e-mail informado no formulário de login.
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email));

        // Retorna um UserDetails do Spring Security contendo o e-mail, senha criptografada e a autoridade (papel/role) com prefixo ROLE_.
        return new User(
                usuario.getEmail(),
                usuario.getSenha(),
                Collections.singletonList(new SimpleGrantedAuthority(usuario.getRole().getAutoridade()))
        );
    }

    // Registra um novo usuário no sistema a partir do DTO de formulário de cadastro.
    // @Transactional: Garante atomicidade (em caso de falha, todas as operações são revertidas).
    @Transactional
    public Usuario cadastrarUsuario(CadastroForm form) {
        // Regra de unicidade: impede o cadastro duplicado de um e-mail já existente no banco.
        if (usuarioRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("O e-mail '" + form.getEmail() + "' já está em uso por outro membro.");
        }

        // Instancia a nova entidade de usuário e preenche com os dados sanitizados.
        Usuario usuario = new Usuario();
        usuario.setNome(form.getNome().trim());
        usuario.setEmail(form.getEmail().trim().toLowerCase());
        // Criptografa a senha antes de salvar no banco via BCrypt.
        usuario.setSenha(passwordEncoder.encode(form.getSenha()));
        // Define o papel padrão inicial de novo membro comum (USR).
        usuario.setRole(Roles.USR);

        // Salva e retorna o usuário persistido com ID gerado.
        return usuarioRepository.save(usuario);
    }

    // Localiza um usuário pelo seu e-mail cadastrado (lança exceção caso não exista).
    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email));
    }

    // Localiza um usuário pelo seu identificador primário (ID numérico).
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado para o ID: " + id));
    }

    // Adiciona uma obra à estante/biblioteca pessoal de livros favoritos do usuário.
    @Transactional
    public void adicionarLivroABiblioteca(Long livroId, Integer usuarioId) {
        // Recupera o usuário e o livro do banco de dados garantindo a existência de ambos.
        Usuario usuario = buscarPorId(usuarioId);
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado para o ID: " + livroId));

        // Adiciona o livro à coleção ManyToMany de favoritos do usuário.
        usuario.getLivrosFavoritos().add(livro);
        usuarioRepository.save(usuario);
    }

    // Remove um livro da estante pessoal de favoritos do usuário.
    @Transactional
    public void removerLivroDaBiblioteca(Long livroId, Integer usuarioId) {
        Usuario usuario = buscarPorId(usuarioId);
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado para o ID: " + livroId));

        // Remove o livro do conjunto de favoritos e persiste a alteração na tabela de junção.
        usuario.getLivrosFavoritos().remove(livro);
        usuarioRepository.save(usuario);
    }

    // Retorna a coleção de livros favoritos pertencentes ao usuário solicitado.
    @Transactional(readOnly = true)
    public Set<Livro> listarLivrosFavoritos(Integer usuarioId) {
        Usuario usuario = buscarPorId(usuarioId);
        return usuario.getLivrosFavoritos();
    }
}
