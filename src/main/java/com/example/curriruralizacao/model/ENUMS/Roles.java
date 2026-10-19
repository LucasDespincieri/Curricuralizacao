package com.example.curriruralizacao.model.ENUMS;

// Declaração do Enum (enumeração), uma estrutura em Java que representa um conjunto fixo e fechado de constantes.
// Propósito: Definir os papéis/perfis de usuário do sistema para fins de autorização e controle de acesso.
public enum Roles {
    ADM, // Papel de Administrador: possui permissões totais no sistema.
    USR, // Papel de Usuário padrão: permissões regulares de operação.
    GST  // Papel de Convidado / Gestor (Guest): permissões restritas ou de visualização.
}
