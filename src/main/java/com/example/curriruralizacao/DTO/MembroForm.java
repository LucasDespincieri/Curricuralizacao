package com.example.curriruralizacao.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Dados do formulário do CRUD de Membros (Administração do Pastor).
// A validação dos campos fica no MembroService (mesmas regras do cadastro do site).
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembroForm {

    private String nome;   // Nome completo (3 a 50 caracteres)
    private String email;  // Só no cadastro: o e-mail é o login e não muda depois
    private String senha;  // Só no cadastro: senha inicial (mínimo 6 caracteres)
    private String papel;  // "PASTOR" ou "MEMBRO"
    private String cidade; // Opcional (junto com a UF)
    private String uf;     // Opcional (junto com a cidade)
}
