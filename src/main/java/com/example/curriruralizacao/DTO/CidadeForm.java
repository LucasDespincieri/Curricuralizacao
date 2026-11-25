package com.example.curriruralizacao.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO para transporte e validação de dados no formulário de cadastro/edição de Cidades (CRUD).
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CidadeForm {

    private Integer id; // ID numérico da cidade (opcional no cadastro, presente na edição)

    @NotBlank(message = "O nome/descrição da cidade é obrigatório")
    @Size(min = 2, max = 50, message = "A cidade deve ter entre 2 e 50 caracteres")
    private String descricao; // Nome da cidade
}
