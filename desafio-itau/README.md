# Desafio Itaú — API de Transações

API REST desenvolvida com Java e Spring Boot para registrar transações
e calcular estatísticas dos últimos 60 segundos.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Maven

## Armazenamento

As transações são armazenadas em memória, sem banco de dados ou cache.
Ao encerrar a aplicação, os dados são perdidos.

## Como executar

É necessário ter o JDK 21 ou superior instalado.

Abra o terminal na pasta que contém o arquivo pom.xml.

No Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em http://localhost:8080.

## Endpoints

### POST /transacao

Recebe um JSON com os campos:

```json
{
  "valor": 10.50,
  "dataHora": "2026-09-30T12:00:00-03:00"
}
```

Regras:
- Os dois campos são obrigatórios.
- O valor deve ser maior ou igual a zero.
- A data deve incluir o fuso horário e não pode estar no futuro.
- Transações antigas são aceitas, mas só as dos últimos
  60 segundos entram nas estatísticas.

Respostas sem corpo:
- 201: transação registrada.
- 422: campos ausentes ou valores que violam as regras.
- 400: JSON malformado ou dados que não podem ser interpretados.

### DELETE /transacao

Apaga todas as transações armazenadas.
Retorna 200 sem corpo.

### GET /estatistica

Retorna as estatísticas das transações dos últimos 60 segundos:

- count: quantidade.
- sum: soma.
- avg: média.
- min: menor valor.
- max: maior valor.

Se não houver transações nesse intervalo, todos os valores são zero.

## Verificações manuais realizadas

- Cadastro de transação válida.
- Rejeição de valor negativo.
- Rejeição de campos obrigatórios ausentes.
- Rejeição de data futura.
- Aceitação de valor zero.
- Retorno 400 para JSON malformado.
- Cálculo das estatísticas com valores 10 e 20.
- Retorno de zeros sem transações recentes.
- Limpeza dos dados pelo DELETE.