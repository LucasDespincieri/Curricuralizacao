package com.example.curriruralizacao.model;

import com.example.curriruralizacao.model.ENUMS.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Marca esta classe como uma entidade gerenciada pelo JPA e mapeada para a tabela 'Usuario' no banco de dados.
@Entity
// Lombok: Gera automaticamente todos os métodos Getters e Setters para permitir o encapsulamento seguro dos dados.
@Getter @Setter
// Lombok: Gera o construtor padrão vazio, indispensável para o Hibernate instanciar a entidade a partir do banco.
@NoArgsConstructor
// Especifica explicitamente o nome da tabela física no banco de dados como "tb_usuario".
@Table(name = "tb_usuario")
public class Usuario {

    // Define 'id' como a chave primária (Primary Key) da entidade.
    @Id
    // Configura a estratégia de auto-incremento da chave primária pelo banco de dados.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Especifica que a coluna física no banco de dados se chama "idUsuario".
    @Column(name = "idUsuario")
    private Integer id; // Identificador numérico único do usuário.

    // Mapeia o atributo para uma coluna comum na tabela.
    @Column
    // Validação do Hibernate: Garante que o nome tenha no máximo 50 caracteres.
    @Length(max = 50, message = "O nome deve ter no máximo 50 carateres")
    private String nome; // Nome completo do usuário.

    // Define a coluna no banco com constraint de unicidade (unique = true), impedindo duplicidade de e-mails.
    @Column(unique = true)
    // Bean Validation: Torna o campo de e-mail estritamente obrigatório (não pode ser nulo).
    @NotNull(message = "O uso do email é obrigatório")
    private String email; // Endereço de e-mail do usuário, usado como identificador de login e contato.

    // Mapeia a coluna no banco para armazenar o hash da senha de autenticação.
    @Column
    // Bean Validation: Torna o preenchimento da senha estritamente obrigatório.
    @NotNull(message = "senha não pode estar vazia")
    private String senha; // Senha criptografada do usuário.

    // Mapeia a coluna no banco para armazenar o bairro.
    @Column
    private String bairro; // Bairro de residência do usuário.

    // Coluna para armazenar a data de nascimento (armazena apenas ano, mês e dia, sem componente de horário).
    @Column
    private LocalDate dataNascimento; // Data de nascimento do usuário.

    // Mapeia o atributo 'dataCadastro' para a coluna física com nome personalizado "dia_cadastro".
    @Column(name = "dia_cadastro")
    private LocalDateTime dataCadastro; // Data e horário exatos em que o registro do usuário foi criado.

    // Relacionamento Muitos-para-Um: muitos usuários podem morar na mesma cidade.
    // FetchType.LAZY: Carregamento sob demanda (lazy loading) para otimizar desempenho de consultas.
    @ManyToOne(fetch = FetchType.LAZY)
    // Especifica a coluna de chave estrangeira (Foreign Key) na tabela Usuario que aponta para Cidade.
    @JoinColumn(name = "idCidade")
    private Cidade cidade; // Objeto Cidade vinculado ao endereço do usuário.

    // Mapeia a coluna no banco de dados para armazenar a Role (enum).
    // @Enumerated(EnumType.STRING) armazena o nome da constante ("ADM", "USR") em vez do índice numérico.
    @Column
    @Enumerated(EnumType.STRING)
    private Roles role; // Perfil ou papel de permissões atribuído a este usuário (ADM, USR, GST).

    // Relacionamento Um-para-Muitos: um usuário pode ser autor de várias publicações (Posts).
    // - cascade = CascadeType.ALL e orphanRemoval = true: se o usuário for removido, seus posts são excluídos em cascata.
    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>(); // Lista de publicações criadas pelo usuário.

    // Relacionamento Um-para-Muitos: um usuário pode publicar múltiplos comentários na plataforma.
    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comentarios> comentarios = new ArrayList<>(); // Lista de comentários publicados pelo usuário.

    // Relacionamento Um-para-Muitos: um usuário pode destacar múltiplos trechos em livros (grifos).
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Grifo> grifos = new ArrayList<>(); // Lista de grifos e anotações pessoais do leitor.

    // Relacionamento Um-para-Muitos: um usuário pode reagir a diversos posts.
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReacaoPost> reacoes = new ArrayList<>(); // Lista de reações enviadas por este usuário.

    // Construtor sobrecarregado com argumentos para facilitar a instanciação de usuários com seus dados principais.
    public Usuario(String nome, String email, String senha, LocalDateTime dataCadastro, String role) {
        this.nome = nome; // Atribui o nome fornecido.
        this.email = email; // Atribui o e-mail fornecido.
        // Se a dataCadastro não for informada (for null), preenche automaticamente com a data e hora do momento atual.
        this.dataCadastro = dataCadastro != null ? dataCadastro : LocalDateTime.now();
        this.senha = senha; // Atribui a senha informada.
        this.role = Roles.valueOf(role); // Converte a String recebida na constante correspondente do Enum Roles.
    }

    // Sobrescrita de equals() para comparar se duas instâncias de Usuario representam o mesmo registro no banco.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Se for exatamente a mesma referência em memória, são iguais.
        if (o == null || getClass() != o.getClass()) return false; // Se for nulo ou classes diferentes, não são iguais.
        Usuario usuario = (Usuario) o; // Faz o cast seguro para Usuario.
        return Objects.equals(id, usuario.id); // Considera iguais se possuírem o mesmo ID primário.
    }

    // Sobrescrita de hashCode() compatível com equals(), baseada no identificador primário.
    @Override
    public int hashCode() {
        return Objects.hash(id); // Gera o código hash a partir do id.
    }
}
