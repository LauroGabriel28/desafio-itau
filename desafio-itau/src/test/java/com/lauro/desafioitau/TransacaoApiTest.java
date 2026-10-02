package com.lauro.desafioitau;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.lauro.desafioitau.service.TransacaoService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransacaoApiTest {

    @LocalServerPort
    private int porta;

    @Autowired
    private TransacaoService service;

    private final HttpClient cliente = HttpClient.newHttpClient();

    @BeforeEach
    void preparar() {
        service.limpar();
    }

    private HttpResponse<String> cadastrar(String json) throws Exception {
        HttpRequest pedido = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + porta + "/transacao"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }

    private String transacao(double valor, OffsetDateTime dataHora) {
        return """
                {"valor":%s,"dataHora":"%s"}
                """.formatted(Double.toString(valor), dataHora);
    }

    @Test
    void aceitaTransacaoValida() throws Exception {
        var resposta = cadastrar(
                transacao(10, OffsetDateTime.now().minusSeconds(5))
        );

        assertEquals(201, resposta.statusCode());
        assertEquals("", resposta.body());
        assertEquals(1, service.calcularEstatistica().count());
    }

    @Test
    void aceitaValorZero() throws Exception {
        var resposta = cadastrar(
                transacao(0, OffsetDateTime.now().minusSeconds(5))
        );

        assertEquals(201, resposta.statusCode());
        assertEquals("", resposta.body());
        assertEquals(1, service.calcularEstatistica().count());
    }

    @Test
    void rejeitaValorNegativo() throws Exception {
        var resposta = cadastrar(
                transacao(-10, OffsetDateTime.now().minusSeconds(5))
        );

        assertEquals(422, resposta.statusCode());
        assertEquals("", resposta.body());
        assertEquals(0, service.calcularEstatistica().count());
    }

    @Test
    void rejeitaDataFutura() throws Exception {
        var resposta = cadastrar(
                transacao(10, OffsetDateTime.now().plusDays(1))
        );

        assertEquals(422, resposta.statusCode());
        assertEquals("", resposta.body());
        assertEquals(0, service.calcularEstatistica().count());
    }

    @Test
    void rejeitaValorAusente() throws Exception {
        var resposta = cadastrar("""
                {"dataHora":"2020-01-01T12:00:00-03:00"}
                """);

        assertEquals(422, resposta.statusCode());
        assertEquals("", resposta.body());
    }

    @Test
    void rejeitaDataAusente() throws Exception {
        var resposta = cadastrar("""
                {"valor":10}
                """);

        assertEquals(422, resposta.statusCode());
        assertEquals("", resposta.body());
    }

    @Test
    void rejeitaJsonInvalido() throws Exception {
        var resposta = cadastrar("{\"valor\":");

        assertEquals(400, resposta.statusCode());
        assertEquals("", resposta.body());
    }
    @Test
    void calculaEstatisticasDeTransacoesRecentes() throws Exception {
        OffsetDateTime data = OffsetDateTime.now().minusSeconds(5);

        assertEquals(201, cadastrar(transacao(10, data)).statusCode());
        assertEquals(201, cadastrar(transacao(20, data)).statusCode());

        var estatistica = service.calcularEstatistica();

        assertEquals(2, estatistica.count());
        assertEquals(30.0, estatistica.sum());
        assertEquals(15.0, estatistica.avg());
        assertEquals(10.0, estatistica.min());
        assertEquals(20.0, estatistica.max());
    }

    @Test
    void aceitaTransacaoAntigaMasNaoIncluiNasEstatisticas() throws Exception {
        var resposta = cadastrar(
                transacao(100, OffsetDateTime.now().minusMinutes(5))
        );

        assertEquals(201, resposta.statusCode());

        var estatistica = service.calcularEstatistica();

        assertEquals(0, estatistica.count());
        assertEquals(0.0, estatistica.sum());
        assertEquals(0.0, estatistica.avg());
        assertEquals(0.0, estatistica.min());
        assertEquals(0.0, estatistica.max());
    }

    @Test
    void retornaEstatisticasZeradasSemTransacoes() throws Exception {
        HttpRequest pedido = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + porta + "/estatistica"))
                .GET()
                .build();

        var resposta = cliente.send(
                pedido, HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, resposta.statusCode());
        assertEquals(
                "application/json",
                resposta.headers().firstValue("Content-Type").orElse("")
        );
        assertEquals(
                "{\"count\":0,\"sum\":0.0,\"avg\":0.0,\"min\":0.0,\"max\":0.0}",
                resposta.body()
        );
    }

    @Test
    void deleteApagaTransacoes() throws Exception {
        assertEquals(
                201,
                cadastrar(transacao(
                        50, OffsetDateTime.now().minusSeconds(5)
                )).statusCode()
        );

        assertEquals(1, service.calcularEstatistica().count());

        HttpRequest pedido = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + porta + "/transacao"))
                .DELETE()
                .build();

        var resposta = cliente.send(
                pedido, HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, resposta.statusCode());
        assertEquals("", resposta.body());
        assertEquals(0, service.calcularEstatistica().count());
    }
}