package com.example.curriruralizacao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Anotação que indica ao Spring Boot Test para carregar o contexto completo da aplicação para os testes integrados.
@SpringBootTest
class CurriruralizacaoApplicationTests {

    // @Test: Anotação do JUnit Jupiter (JUnit 5) que marca este método como um teste unitário/integrado a ser executado.
    @Test
    void contextLoads() {
        // Teste de integridade de contexto (smoke test): verifica se todas as configurações, beans e entidades
        // do Spring Boot conseguem ser inicializados sem lançar exceções.
    }

}
