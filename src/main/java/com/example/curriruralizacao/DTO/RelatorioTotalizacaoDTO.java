package com.example.curriruralizacao.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO para representar o resultado da Consulta com Dados em Múltiplas Tabelas com Totalização
// Realiza JOIN entre Usuario, Post, Comentarios e ReacaoPost com funções de agregação (COUNT).
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioTotalizacaoDTO {

    private Integer usuarioId;      // Identificador do usuário
    private String autorNome;       // Nome do membro
    private String email;           // E-mail do membro
    private Long totalPosts;        // Total de postagens publicadas pelo membro
    private Long totalComentarios;  // Total de comentários realizados pelo membro
    private Long totalReacoes;      // Total de reações emitidas pelo membro
}
