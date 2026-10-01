package com.lauro.desafioitau.service;

import com.lauro.desafioitau.model.Transacao;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransacaoService {

    private final List<Transacao> transacoes = new ArrayList<>();

    public synchronized void adicionar(Transacao transacao) {
        transacoes.add(transacao);
    }
}