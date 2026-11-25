package com.example.curriruralizacao.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO para representar o resultado da Consulta de Pivot (Crosstab)
// Rotaciona as linhas de tipos de reação ("AMEM", "CURTIR", "ORANDO") em colunas específicas por postagem.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioPivotDTO {

    private Long postId;              // ID da postagem
    private String postTrecho;        // Trecho inicial do conteúdo da postagem
    private String autorNome;         // Nome do autor da postagem
    private Long totalAmem;           // Coluna pivotada: total de reações do tipo "AMEM"
    private Long totalCurtir;         // Coluna pivotada: total de reações do tipo "CURTIR"
    private Long totalOrando;         // Coluna pivotada: total de reações do tipo "ORANDO"
    private Long totalReacoesGerais;  // Soma total de todas as reações recebidas pelo post
}
