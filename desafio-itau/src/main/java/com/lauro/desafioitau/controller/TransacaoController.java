package com.lauro.desafioitau.controller;

import com.lauro.desafioitau.model.Transacao;
import com.lauro.desafioitau.service.TransacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.OffsetDateTime;

@RestController
public class TransacaoController {

    private final TransacaoService service;

    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    @PostMapping("/transacao")
    public ResponseEntity<Void> adicionar(@RequestBody Transacao transacao) {

        if (transacao.getValor() == null
                || transacao.getDataHora() == null
                || !Double.isFinite(transacao.getValor())
                || transacao.getValor() < 0
                || transacao.getDataHora().isAfter(OffsetDateTime.now())) {

            return ResponseEntity.status(422).build();
        }

        service.adicionar(transacao);

        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/transacao")
    public ResponseEntity<Void> limpar() {
        service.limpar();
        return ResponseEntity.ok().build();
    }
}