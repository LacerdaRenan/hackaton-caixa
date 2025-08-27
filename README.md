# Simulador de Crédito API

---

## Sobre o Projeto

O **Simulador de Crédito API** é uma solução desenvolvida para o Hackathon 2025 da Caixa Econômica Federal, com o objetivo de fornecer um serviço robusto e escalável para simulações de financiamento, e, com  isso, disponibilizar para todos os brasileiros a possibilidade de simulação de empréstimo. A API permite calcular financiamentos pelos sistemas **SAC (Sistema de Amortização Constante)** e **Price**, além de oferecer endpoints para monitoramento e análise de dados.

Este projeto foi construído utilizando **Quarkus**, pois o mesmo disponibiliza uma arquitetura de microsserviços moderna, garantindo alta performance, resiliência e observabilidade.

---

## Funcionalidades Principais

-   **Simulação de Financiamento:** Crie simulações detalhadas para diferentes produtos de crédito.
-   **Consulta de Simulações:** Acesse o histórico completo de todas as simulações realizadas.
-   **Análise de Volume:** Monitore o volume de simulações agrupadas por produto e por dia.
-   **Monitoramento e Telemetria:** Obtenha dados de telemetria (como tempo de resposta e status) para cada serviço.
-   **Gestão de Produtos:** Liste os produtos de financiamento disponíveis para simulação.

---

## Tecnologias Utilizadas

Este projeto foi construído com as seguintes tecnologias:

| Ferramenta                  | Descrição |
|:----------------------------| :--- |
| **Java 21**                 | Linguagem de programação principal. |
| **Quarkus**                 | Framework Java nativo para nuvem, otimizado para alta performance e baixo consumo de memória. |
| **MySQL**                   | Banco de dados relacional para persistência dos dados. |
| **Caffeine (via Quarkus Cache)** | Biblioteca de cache de alta performance para otimização de consultas. |
| **Docker & Docker Compose** | Ferramentas para criação de contêineres e orquestração do ambiente de desenvolvimento. |
| **RESTful API**             | Arquitetura para a comunicação entre cliente e servidor. |

---

## Performance e Escalabilidade

Considerando que este serviço foi projetado para ser utilizado por um grande número de usuários simultaneamente, a performance é fundamental. Para evitar sobrecarga na infraestrutura, especialmente no banco de dados, foi implementada uma estratégia de cache para a consulta de produtos de financiamento.

-   **Estratégia de Cache:** A lista de produtos, que é uma informação consultada com frequência e raramente alterada, é armazenada em cache na memória da aplicação por **60 minutos** utilizando a extensão `quarkus-cache` com Caffeine.
-   **Redução de Latência:** Isso reduz drasticamente o número de chamadas ao banco de dados, diminuindo a latência do endpoint `POST /v1/simulacao`.
-   **Atualização Manual:** Para garantir que o sistema possa refletir alterações nos produtos antes da expiração automática do cache, foi criado um endpoint administrativo (`POST /admin/produto/refresh`) que permite a invalidação forçada do cache.

---

## Qualidade e Testes

A qualidade do código e a confiabilidade do serviço são prioridades neste projeto. Para garantir que a aplicação se comporte como esperado e que a lógica de negócio esteja correta, foram implementados testes automatizados utilizando **JUnit 5**.

-   **Testes Unitários:** Foram criados para validar o comportamento de componentes críticos, como os serviços de cálculo de simulação e as regras de negócio, de forma isolada.
-   **Testes Parametrizados:** Para garantir a robustez dos cálculos de financiamento (SAC e Price), utilizamos testes parametrizados. Essa abordagem nos permitiu testar uma vasta gama de cenários com diferentes entradas (valores, prazos, taxas) de forma eficiente, assegurando a precisão dos resultados.

---

## Como Rodar o Projeto

Siga os passos abaixo para executar o projeto em seu ambiente local.

### Pré-requisitos

-   [Docker](https://www.docker.com/get-started) instalado
-   [Docker Compose](https://docs.docker.com/compose/install/) instalado
-   Um cliente de API, como [Postman](https://www.postman.com/) ou [Insomnia](https://insomnia.rest/) se desejar, mas o serviço possui swagger-ui.

### Passos para Execução

1.  **Descompacte o arquivo:**
    Extraia o conteúdo do arquivo `.zip` do projeto em um diretório de sua preferência.

2.  **Acesse o diretório do projeto:**
    Abra um terminal ou prompt de comando e navegue até a pasta que você acabou de extrair. O terminal deve estar na mesma pasta do arquivo **docker-compose.yml**
    ```bash
    cd caminho/para/projeto
    ```

3.  **Suba os contêineres com Docker Compose:**
    Ao encontrar o arquivo docker-compose.yml, execute o comando a seguir no terminal. Ele irá construir as imagens e iniciar a aplicação Quarkus junto com o banco de dados MySQL.
    ```bash
    docker compose up --build
    ```

4.  **Aguarde a inicialização:**
    Aguarde até que os logs indiquem que a aplicação Quarkus foi iniciada com sucesso. Geralmente, você verá uma mensagem como: `Listening on: http://0.0.0.0:8080`.

- Ao finalizar o build, a API estará disponível em `http://localhost:8080/api`.
- Também está disponível o swagger em `http://localhost:8080/q/swagger-ui`

---

### Parando o Serviço

Para parar e remover todos os containers relacionados ao projeto, execute o seguinte comando no mesmo diretório:

```bash
docker-compose down
```

---

## Documentação da API

A seguir estão detalhados os endpoints disponíveis na aplicação.

**URL Base:** `http://localhost:8080/api`

### 1. Criar Simulação

Cria uma nova simulação de financiamento.

-   **Método:** `POST`
-   **Endpoint:** `/v1/simulacao`
-   **Body (Exemplo):**
    ```json
    {
      "valorDesejado": 1000.00,
      "prazo": 3
    }
    ```
-   **Resposta (Sucesso - 201 Created):**
    ```json
    {
      "idSimulacao": 25,
      "codigoProduto": 1,
      "descricaoProduto": "Produto 1",
      "taxaJuros": 0.0179,
      "resultadoSimulacao": [
        {
          "tipo": "SAC",
          "parcelas": [
            {
              "numero": 1,
              "valorAmortizacao": 500.00,
              "valorJuros": 17.90,
              "valorPrestacao": 517.90
            },
            {
              "numero": 2,
              "valorAmortizacao": 500.00,
              "valorJuros": 8.95,
              "valorPrestacao": 508.95
            }
          ]
        },
        {
          "tipo": "PRICE",
          "parcelas": [
            {
              "numero": 1,
              "valorAmortizacao": 495.56,
              "valorJuros": 17.90,
              "valorPrestacao": 513.46
            },
            {
              "numero": 2,
              "valorAmortizacao": 504.44,
              "valorJuros": 9.03,
              "valorPrestacao": 513.46
            }
          ]
        }
      ]
    }

    ```




### 2. Listar Todas as Simulações

Retorna uma lista paginada de todas as simulações já realizadas.

-   **Método:** `GET`
-   **Endpoint:** `/v1/simulacao?pagina=2&tamanhoPagina=7`
-   **Parâmetros de Query:**
    -   `pagina` (opcional): Número da página (Default 1).
    -   `tamanhoPagina` (opcional): Quantidade de itens por página (Default 5).
-   **Resposta (Sucesso - 200 OK):**
    ```json
    {
      "pagina": 2,
      "qtdRegistros": 25,
      "qtdRegistrosPagina": 7,
      "registros": [
        {
          "idSimulacao": 8,
          "valorDesejado": 50000.00,
          "prazo": 36,
          "valorTotalParcelas": 66187.50
        },
        {
          "idSimulacao": 9,
          "valorDesejado": 10000.01,
          "prazo": 25,
          "valorTotalParcelas": 12275.00
        },
        {
          "idSimulacao": 10,
          "valorDesejado": 100000.00,
          "prazo": 48,
          "valorTotalParcelas": 142874.96
        },
        {
          "idSimulacao": 11,
          "valorDesejado": 500000.00,
          "prazo": 72,
          "valorTotalParcelas": 832150.00
        },
        {
          "idSimulacao": 12,
          "valorDesejado": 500000.00,
          "prazo": 72,
          "valorTotalParcelas": 832150.00
        },
        {
          "idSimulacao": 13,
          "valorDesejado": 500000.00,
          "prazo": 72,
          "valorTotalParcelas": 832150.00
        },
        {
          "idSimulacao": 14,
          "valorDesejado": 100000.01,
          "prazo": 49,
          "valorTotalParcelas": 145500.04
        }
      ]
    }
    ```

### 3. Listar Volume de Simulações

Retorna o volume de simulações agrupadas por produto e por dia.

-   **Método:** `GET`
-   **Endpoint:** `v1/simulacao/AAAA-MM-dd`
-   **Resposta (Sucesso - 200 OK):**
    ```json
    {
      "dataReferencia": "2025-08-27",
      "simulacoes": [
        {
          "codigoProduto": 1,
          "descricaoProduto": "Produto 1",
          "taxaMediaJuro": 0.0179,
          "valorMedioPrestacao": 422.13,
          "valorTotalDesejado": 20200.00,
          "valorTotalCredito": 23604.62
        },
        {
          "codigoProduto": 2,
          "descricaoProduto": "Produto 2",
          "taxaMediaJuro": 0.0175,
          "valorMedioPrestacao": 1932.78,
          "valorTotalDesejado": 180000.01,
          "valorTotalCredito": 247812.46
        },
        {
          "codigoProduto": 3,
          "descricaoProduto": "Produto 3",
          "taxaMediaJuro": 0.0182,
          "valorMedioPrestacao": 15937.91,
          "valorTotalDesejado": 3900000.02,
          "valorTotalCredito": 6863870.08
        },
        {
          "codigoProduto": 4,
          "descricaoProduto": "Produto 4",
          "taxaMediaJuro": 0.0151,
          "valorMedioPrestacao": 21425.96,
          "valorTotalDesejado": 6000000.02,
          "valorTotalCredito": 11134000.04
        }
      ]
    }
    ```

### 4. Listar Dados de Telemetria

Retorna dados de telemetria por data, como tempo médio de resposta e contagem de requisições por endpoint.

-   **Método:** `GET`
-   **Endpoint:** `v1/telemetria/AAAA-MM-dd`
-   **Resposta (Sucesso - 200 OK):**
    ```json
    {
      "dataReferencia": "2025-08-27",
      "listaEndpoints": [
        {
          "nomeApi": "GET /api/v1/produto",
          "qtdRequisicoes": 42,
          "tempoMedio": 9.381,
          "tempoMinimo": 0,
          "tempoMaximo": 250,
          "percentualSucesso": 1.0
        },
        {
          "nomeApi": "POST /api/v1/admin",
          "qtdRequisicoes": 4,
          "tempoMedio": 0.5,
          "tempoMinimo": 0,
          "tempoMaximo": 2,
          "percentualSucesso": 1.0
        },
        {
          "nomeApi": "GET /api/v1/telemetria",
          "qtdRequisicoes": 14,
          "tempoMedio": 22.6429,
          "tempoMinimo": 1,
          "tempoMaximo": 293,
          "percentualSucesso": 1.0
        },
        {
          "nomeApi": "GET /api/v1/simulacao",
          "qtdRequisicoes": 3,
          "tempoMedio": 5.0,
          "tempoMinimo": 3,
          "tempoMaximo": 8,
          "percentualSucesso": 1.0
        },
        {
          "nomeApi": "POST /api/v1/simulacao",
          "qtdRequisicoes": 39,
          "tempoMedio": 7.5385,
          "tempoMinimo": 0,
          "tempoMaximo": 69,
          "percentualSucesso": 0.51282
        }
      ]
    }
    ```

### 5. Listar Produtos de Financiamento

Retorna a lista de produtos disponíveis para simulação.

-   **Método:** `GET`
-   **Endpoint:** `v1/produto`
-   **Resposta (Sucesso - 200 OK):**
    ```json
    [
      {
        "codigoProduto": 1,
        "nomeProduto": "Produto 1",
        "taxaJuros": 0.017900000,
        "numeroMinimoMeses": 0,
        "numeroMaximoMeses": 24,
        "valorMinimo": 200.00,
        "valorMaximo": 10000.00
      },
      {
        "codigoProduto": 2,
        "nomeProduto": "Produto 2",
        "taxaJuros": 0.017500000,
        "numeroMinimoMeses": 25,
        "numeroMaximoMeses": 48,
        "valorMinimo": 10000.01,
        "valorMaximo": 100000.00
      },
      {
        "codigoProduto": 3,
        "nomeProduto": "Produto 3",
        "taxaJuros": 0.018200000,
        "numeroMinimoMeses": 49,
        "numeroMaximoMeses": 96,
        "valorMinimo": 100000.01,
        "valorMaximo": 1000000.00
      },
      {
        "codigoProduto": 4,
        "nomeProduto": "Produto 4",
        "taxaJuros": 0.015100000,
        "numeroMinimoMeses": 97,
        "numeroMaximoMeses": null,
        "valorMinimo": 1000000.01,
        "valorMaximo": null
      }
    ]
    ```
---

### 6. (Admin) Forçar Atualização do Cache de Produtos

Invalida o cache de produtos, forçando a próxima consulta a buscar os dados mais recentes do banco. **Nota:** Teoricamente este endpoint deve ser protegido e acessível apenas por administradores, mas está fora do escopo do projeto.

-   **Método:** `POST`
-   **Endpoint:** `/admin/produto/refresh`
-   **Body:** Nenhum.
-   **Resposta (Sucesso - 200):** Retorna a mensagem de sucesso `Cache de produtos atualizado com sucesso`

---

 **Autor: Renan Lacerda**

