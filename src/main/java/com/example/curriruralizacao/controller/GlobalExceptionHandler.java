package com.example.curriruralizacao.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("erro", "Requisição Inválida");
        model.addAttribute("mensagem", ex.getMessage());
        return "error/500";
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleSecurityException(SecurityException ex, Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("erro", "Acesso Negado");
        model.addAttribute("mensagem", ex.getMessage());
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("status", 500);
        model.addAttribute("erro", "Erro Interno do Servidor");
        model.addAttribute("mensagem", "Ocorreu um erro inesperado ao processar sua solicitação.");
        return "error/500";
    }
}
