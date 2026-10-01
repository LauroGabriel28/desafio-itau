package com.lauro.desafioitau.service;

import com.lauro.desafioitau.model.Estatistica;
import com.lauro.desafioitau.model.Transacao;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

@Service
public class TransacaoService {

    private final List<Transacao> transacoes = new ArrayList<>();

    public synchronized void adicionar(Transacao transacao) {
        transacoes.add(transacao);
    }

    public synchronized void limpar() {
        transacoes.clear();
    }

    public synchronized Estatistica calcularEstatistica() {
        OffsetDateTime agora = OffsetDateTime.now();
        OffsetDateTime limite = agora.minusSeconds(60);

        DoubleSummaryStatistics resumo = transacoes.stream()
                .filter(transacao ->
                        !transacao.getDataHora().isBefore(limite)
                                && !transacao.getDataHora().isAfter(agora))
                .mapToDouble(Transacao::getValor)
                .summaryStatistics();

        if (resumo.getCount() == 0) {
            return new Estatistica(0, 0, 0, 0, 0);
        }

        return new Estatistica(
                resumo.getCount(),
                resumo.getSum(),
                resumo.getAverage(),
                resumo.getMin(),
                resumo.getMax()
        );
    }
}