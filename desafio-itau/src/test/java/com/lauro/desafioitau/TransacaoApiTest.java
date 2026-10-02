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
}