package com.lauro.desafioitau.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratamentoErros {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Void> tratarJsonInvalido() {
        return ResponseEntity.badRequest().build();
    }
}