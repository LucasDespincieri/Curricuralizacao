package com.example.curriruralizacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication: Anotação fundamental do Spring Boot que combina três anotações essenciais:
// 1. @Configuration: marca a classe como fonte de definições de beans no contexto da aplicação.
// 2. @EnableAutoConfiguration: habilita o mecanismo de auto-configuração do Spring Boot baseado nas dependências do pom.xml.
// 3. @ComponentScan: ativa a busca automática de componentes Spring (@Component, @Service, @Repository, @Controller, etc.) neste pacote e subpacotes.
@SpringBootApplication
public class CurriruralizacaoApplication {

    // Método main: ponto de entrada padrão da aplicação Java.
    public static void main(String[] args) {
        // Inicializa o contêiner de Inversão de Controle (IoC) do Spring, configura o contexto da aplicação
        // e inicia o servidor web embutido (caso dependências web estejam presentes).
        SpringApplication.run(CurriruralizacaoApplication.class, args);
    }

}
