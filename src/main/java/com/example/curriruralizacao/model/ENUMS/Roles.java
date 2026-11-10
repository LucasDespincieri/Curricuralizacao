package com.example.curriruralizacao.model.ENUMS;

// Declaração do Enum (enumeração), uma estrutura em Java que representa um conjunto fixo e fechado de constantes.
// Propósito: Definir os papéis/perfis de usuário do sistema para fins de autorização e controle de acesso.
public enum Roles {
    ADM, // Papel de Administrador (Pastor): possui permissões administrativas totais no sistema (ex: gerenciar livros e moderação).
    USR, // Papel de Usuário padrão (Membro da igreja): permissões regulares de publicação, comentário e leitura.
    GST; // Papel de Convidado / Gestor (Guest): permissões restritas ou de visualização.

    // Método utilitário que formata o papel no padrão exigido pelo Spring Security adicionando o prefixo "ROLE_".
    // Exemplo: ADM vira "ROLE_ADM", USR vira "ROLE_USR".
    public String getAutoridade() {
        return "ROLE_" + this.name();
    }
}
