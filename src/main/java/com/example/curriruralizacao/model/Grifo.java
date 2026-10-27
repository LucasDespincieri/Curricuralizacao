package com.example.curriruralizacao.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// @Entity: Mapeia esta classe como uma entidade gerenciada pelo Hibernate/JPA.
@Entity
// @Table: Define o nome da tabela no banco como "tb_grifo".
@Table(name = "tb_grifo")
// Lombok: Gera automaticamente métodos getters e setters de todos os atributos.
@Getter @Setter
// Lombok: Gera equals() e hashCode() customizados.
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Grifo {

    // Define a chave primária da entidade Grifo.
    @Id
    // Configura geração automática do identificador via auto-incremento pelo banco de dados.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador numérico único da marcação/grifo.

    // Coluna para armazenar o trecho do texto do capítulo que o leitor destacou/grifou (obrigatório).
    @Column(name = "texto_selecionado", nullable = false, columnDefinition = "TEXT")
    private String textoSelecionado; // Trecho destacado da leitura.

    // Coluna opcional para uma anotação, reflexão ou comentário pessoal sobre o texto grifado.
    @Column(columnDefinition = "TEXT")
    private String anotacao; // Nota pessoal associada ao trecho grifado.

    // Data e horário em que o grifo foi registrado (não nulo e imutável após criação).
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao; // Timestamp de realização do grifo.

    // Relacionamento Muitos-para-Um: muitos grifos são feitos em um mesmo Capítulo.
    @ManyToOne(fetch = FetchType.LAZY)
    // Chave estrangeira que aponta para o capítulo correspondente.
    @JoinColumn(name = "capitulo_id", nullable = false)
    private Capitulo capitulo; // Capítulo onde o texto foi destacado.

    // Relacionamento Muitos-para-Um: muitos grifos pertencem a um mesmo Usuário leitor.
    @ManyToOne(fetch = FetchType.LAZY)
    // Chave estrangeira que aponta para o usuário que realizou a marcação.
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // Usuário proprietário do grifo.

    // Construtor vazio padrão, exigido pela especificação JPA.
    public Grifo() {
    }

    // Construtor completo para facilitar a criação de instâncias de grifos.
    public Grifo(String textoSelecionado, String anotacao, Capitulo capitulo, Usuario usuario) {
        this.textoSelecionado = textoSelecionado;
        this.anotacao = anotacao;
        this.capitulo = capitulo;
        this.usuario = usuario;
    }

    // Callback de ciclo de vida do JPA garantindo a definição de dataCriacao antes de persistir.
    @PrePersist
    protected void onCreate() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
    }

}
