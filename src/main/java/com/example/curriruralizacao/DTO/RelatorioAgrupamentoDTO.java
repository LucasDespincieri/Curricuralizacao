package com.example.curriruralizacao.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO para representar o resultado da Consulta de Agrupamento (GROUP BY + HAVING)
// Agrupa as obras literárias e capítulos pelo autor cadastrado.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioAgrupamentoDTO {

    private String autorNome;      // Nome do pastor / autor
    private Long totalLivros;      // Quantidade de livros publicados pelo autor
    private Long totalCapitulos;   // Quantidade total de capítulos somados de todas as obras
}
