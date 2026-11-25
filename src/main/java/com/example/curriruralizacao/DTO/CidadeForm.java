package com.example.curriruralizacao.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Dados do formulário de cadastro/edição de Cidades (CRUD da Administração).
// A validação (nome com 2 a 50 caracteres, UF de um estado brasileiro, sem duplicar) fica no CidadeService.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CidadeForm {

    private String nome; // Nome da cidade (ex.: "Assis")
    private String uf;   // Sigla do estado (ex.: "SP")
}
