package com.example.curriruralizacao.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// Tratador de erros só da API (/api/**): responde em JSON {"error": "..."}, que é o que o Angular lê.
// Sem ele, os erros caíam no GlobalExceptionHandler (feito para as telas Thymeleaf), que tenta abrir
// a página "error/500" — que não existe — e o front recebia um erro 500 genérico.
@RestControllerAdvice(basePackages = "com.example.curriruralizacao.controller.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> dadosInvalidos(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> semPermissao(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflito(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> corpoInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Dados enviados em formato inválido."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> erroInesperado(Exception ex) {
        // Erros do próprio Spring (rota sem o método HTTP, parâmetro faltando...) já trazem o status certo.
        if (ex instanceof ErrorResponse erro) {
            return ResponseEntity.status(erro.getStatusCode()).body(Map.of("error", "Requisição inválida."));
        }
        log.error("Erro inesperado na API", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro inesperado no servidor. Tente novamente."));
    }
}
