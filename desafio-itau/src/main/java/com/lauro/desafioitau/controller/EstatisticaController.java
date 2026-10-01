package com.lauro.desafioitau.controller;

import com.lauro.desafioitau.model.Estatistica;
import com.lauro.desafioitau.service.TransacaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EstatisticaController {

    private final TransacaoService service;

    public EstatisticaController(TransacaoService service) {
        this.service = service;
    }

    @GetMapping("/estatistica")
    public Estatistica calcular() {
        return service.calcularEstatistica();
    }
}