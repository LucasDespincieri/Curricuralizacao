package com.example.curriruralizacao.service;

import org.springframework.stereotype.Service;

// @Service: Anotação que identifica esta classe como um componente da camada de serviço (regras de negócio) no Spring.
// Propósito: Conter a lógica de negócio da aplicação relacionada a Usuários, atuando como intermediária
// entre os controladores (controllers/web) e os repositórios (acesso ao banco de dados).
@Service
public class UsuarioService {

    // Esta classe está pronta para receber a injeção do UsuarioRepository e conter métodos como:
    // - cadastrarUsuario(Usuario usuario)
    // - buscarPorId(Integer id)
    // - listarTodos()
    // - atualizarUsuario(Usuario usuario)
    // - deletarUsuario(Integer id)

}
