# Documentação do Projeto — Tema 5
## Gestão de Erros como Design de Software

**INF0288 — Linguagens e Paradigmas de Programação (2026.1 — TC)**  
**Docente:** Eduardo Simões de Albuquerque  
**Instituto de Informática — Universidade Federal de Goiás**

**Grupo:**
- Aisha Neves de Andrade
- José Jeovah dos Reis Neto
- Marina Pereira Alves
- Rosane Martins de Oliveira
- Viviany Ribeiro Batista

**Junho de 2026**

---

## Visão Geral das Entregas

| Semana | Entrega | Data | Status |
|---|---|---|---|
| Semana 0 | Definição do grupo e tema provisório | 05/05/2026 | CONCLUÍDO |
| Semana 1 | Documento de definição do projeto | 12/05/2026 | CONCLUÍDO |
| Semana 2 | Esboço do Relatório Técnico | 19/05/2026 | CONCLUÍDO |
| Semana 3 | Protótipo Alpha (código parcial) | 26/05/2026 | CONCLUÍDO |
| Semanas 4–5 | Rascunho Relatório Final + Código | 09/06/2026 | EM ANDAMENTO — código concluído, slides pendentes |
| Semana 6 | Relatório Final + Código + Slides + Apresentação | 23/06/2026 | PENDENTE |

---

## SEMANA 0 — Preparação
**05/05/2026 · CONCLUÍDO**

### O que era esperado
- **Entrega:** documento com a definição do grupo.
- **Atividade:** escolha provisória do tema e definição das linguagens.
- **Objetivo:** garantir que o grupo entendeu o contexto do trabalho.

### Entrega realizada

- **Membros do grupo:** Aisha, José Jeovah, Marina, Rosane, Viviany
- **Total de integrantes:** 5 estudantes
- **Tema escolhido (provisório):** Tema 5 — Gestão de Erros como Design de Software
- **Linguagens definidas:** Java (Exceções) vs. Rust (Result\<T, E>)

### Capítulos do Sebesta identificados

- **Capítulo 14** — Tratamento de Exceções (núcleo do tema)
- **Capítulo 5** — Tipos de Dados (base para Tipos de Soma)
- **Capítulo 2** — Evolução das Linguagens (contexto de confiabilidade)

> O grupo formou-se com 5 integrantes e definiu provisoriamente o Tema 5, com as linguagens Java e Rust como alvo da comparação. A escolha foi confirmada na Semana 1 sem alterações.

---

## SEMANA 1 — Fundamentação e Escopo
**12/05/2026 · CONCLUÍDO**

### O que era esperado
- **Entrega:** documento de definição do projeto.
- **Atividade:** escolha definitiva do tema e listagem dos capítulos do Sebesta.
- **Objetivo:** garantir que o grupo entendeu o problema técnico antes de programar.

### Definição definitiva do projeto

- **Tema:** Tema 5 — Gestão de Erros como Design de Software
- **Linguagem A:** Java — modelo de Exceções (`try`/`catch`/`throw`)
- **Linguagem B:** Rust — modelo de Tipos de Soma (`Result<T, E>`)

### Problema técnico central

O projeto simula um sistema de transferências bancárias que deve lidar com falhas como saldo insuficiente, conta inexistente e indisponibilidade de rede. O objetivo é demonstrar como Java permite que erros sejam ignorados com `catch` vazio, enquanto Rust obriga o tratamento em tempo de compilação.

### Aspectos teóricos mandatórios — Sebesta

| Conceito | Capítulo |
|---|---|
| Tratamento de Exceções: mecanismo de lançamento, captura e propagação | Cap. 14 |
| Exceções Verificadas vs. Não Verificadas: distinção crucial para o caso Java | Cap. 14 |
| Tipos de Soma / `Result<T, E>`: o erro como valor de retorno tipado | Cap. 5 |
| Confiabilidade de Linguagens: como o design previne falhas | Cap. 2 |
| Vinculação de Tipo Estática: verificação de erros em tempo de compilação (Rust) | Cap. 6 |

### Escopo do protótipo

- Sistema de transferência bancária: depositar, sacar e transferir
- Cenários de falha: saldo insuficiente, conta não encontrada, valor negativo, timeout de rede
- Implementação paralela em Java e Rust para comparação direta
- Demonstração do `catch` vazio em Java e impossibilidade em Rust

---

## SEMANA 2 — Design e Teoria
**19/05/2026 · CONCLUÍDO**

### O que era esperado
- **Entrega:** esboço do Relatório Técnico (Seção de Fundamentos).
- **Atividade:** escrita da análise teórica sobre os paradigmas envolvidos.
- **Objetivo:** nivelar o conhecimento teórico do grupo.

### 2.1 O Modelo de Exceções — Java (Sebesta Cap. 14)

O mecanismo de exceções em Java é baseado na interrupção do fluxo normal de execução. Quando uma condição anormal ocorre, um objeto de exceção é lançado (`throw`) e o controle é transferido ao bloco `catch` correspondente na pilha de chamadas — conceito denominado pelo Sebesta de **propagação de exceções**.

Java divide as exceções em duas categorias: **verificadas** (*checked*), que o compilador exige tratamento explícito, e **não verificadas** (*unchecked*), que podem ser ignoradas. O problema central é que mesmo exceções verificadas podem ser capturadas por um bloco `catch` vazio — tornando o erro invisível sem nenhum aviso do compilador.

### 2.2 O Modelo de Tipos de Soma — Rust (Sebesta Cap. 5)

Em Rust, o tratamento de erros é parte integral do sistema de tipos. Funções que podem falhar retornam `Result<T, E>` — um **Tipo de Soma** com duas variantes: `Ok(T)` em caso de sucesso, ou `Err(E)` em caso de falha. O compilador rejeita código que ignora um `Result`, eliminando erros silenciosos por design.

O atributo `#[must_use]` instrui o compilador a emitir erro caso o `Result` seja ignorado, tornando o descarte de erros visível em tempo de compilação — e não apenas em tempo de execução como no Java.

### 2.3 Confiabilidade de Linguagens — Sebesta Cap. 2

Sebesta define **confiabilidade** como a propriedade de um programa se comportar conforme sua especificação em todas as condições. A distinção central é que Java delega a confiabilidade à **disciplina do programador**, enquanto Rust embute a confiabilidade no **sistema de tipos** — tornando certos bugs impossíveis por design.

---

## SEMANA 3 — Desenvolvimento e Prova de Conceito
**26/05/2026 · CONCLUÍDO**

### O que era esperado
- **Entrega:** Protótipo Alpha (código parcial).
- **Atividade:** implementação da funcionalidade central nas duas linguagens.
- **Objetivo:** validar a parte prática e resolver impedimentos técnicos.

### Estrutura do projeto implementado

**Versão Java (`java-prototype/`)**
```
java-prototype/
├── run.sh                       (script de execução: compila e roda com java 21)
└── src/main/java/banking/
    ├── TransferException.java   (hierarquia de exceções tipadas)
    ├── Account.java             (modelo da conta bancária)
    ├── Bank.java                (lógica de transferências + catch vazio)
    ├── StressTests.java         (7 cenários de teste)
    └── Main.java                (ponto de entrada)
```

**Versão Rust (`rust-prototype/`)**
```
rust-prototype/
├── run.sh                   (script de execução: requer cargo instalado)
├── Cargo.toml
└── src/
    ├── main.rs              (ponto de entrada)
    ├── domain.rs            (Account, Bank, TransferError enum)
    └── stress_tests.rs      (7 cenários de teste)
```

### Cenários implementados

| Teste | Cenário |
|---|---|
| Teste 1 | Saldo insuficiente |
| Teste 2 | Conta inexistente (ACC-999) |
| Teste 3 | Valor negativo |
| Teste 4 | Retry de rede — sucesso na 3ª tentativa |
| Teste 5 | Retry esgotado — NetworkTimeout |
| Teste 6 | Transferências sequenciais — conservação de R$1.700,00 |
| Teste 7 | Blast radius — erros não contaminam operações seguintes |

### Demonstração do ponto central

O método `transferSemTratar()` em `Bank.java` demonstra o *catch* vazio: o mesmo erro que causaria rejeição em Rust é silenciado sem nenhum aviso. O compilador Java aceita o código; o compilador Rust rejeitaria.

```java
// Java — COMPILA SEM AVISO. O erro desaparece silenciosamente.
public void transferSemTratar(String from, String to, double amount) {
    try {
        transfer(from, to, amount);
    } catch (TransferException e) {
        // catch vazio — o compilador aceita isto sem emitir nenhum aviso
    }
}
```

```rust
// Rust — NÃO COMPILA.
// error[E0282]: unused `Result` that must be used
bank.transfer("ACC-001", "ACC-002", 9999.0);
```

---

## SEMANAS 4 E 5 — Finalização e Ensaio
**até 09/06/2026 · EM ANDAMENTO**

### O que é esperado
- **Entrega:** rascunho do Relatório Final + Código.
- **Atividade:** relatório comparativo, polimento do código e ensaio da apresentação.
- **Objetivo:** preparação para os entregáveis finais.

### Progresso atual (02/06/2026)

| Item | Status |
|---|---|
| Código Java — polimento e demonstração do catch vazio (`transferSemTratar`) | CONCLUÍDO |
| Código Rust — demonstração do `#[must_use]` e contraste com `let _ = ...` | CONCLUÍDO |
| `README.md` — documentação técnica completa do projeto | CONCLUÍDO |
| `DOCUMENTACAO.md` — relatório de acompanhamento com todas as semanas | CONCLUÍDO |
| Guia de perguntas técnicas (10 perguntas) | CONCLUÍDO |
| Slides da apresentação (10 slides) | PENDENTE |
| Ensaio completo cronometrado | PENDENTE |
| Divisão das partes por membro | PENDENTE |

### Relatório Comparativo

#### Estrutura do tipo de erro

Em Java, cada tipo de erro é uma classe que herda de `TransferException` (que por sua vez herda de `Exception`). Em Rust, todos os erros são variantes de um único `enum TransferError` — o Tipo de Soma do Sebesta. O compilador Rust garante via *pattern matching* que todas as variantes sejam tratadas.

#### Assinatura das funções

Em Java o erro é **parcialmente explícito**: declarado na cláusula `throws`, mas ausente do tipo de retorno. Em Rust o erro é **totalmente explícito**: faz parte do tipo de retorno `Result<f64, TransferError>`, visível a qualquer chamador. Isso é o que o Sebesta chama de **vinculação de tipo estática** — o erro é verificado em compilação, não em execução.

#### O catch vazio — argumento central

O `catch` vazio em Java compila e executa sem nenhum aviso. O equivalente em Rust é **impossível**: o atributo `#[must_use]` faz o compilador recusar código que ignora um `Result`. Esta é a evidência empírica central do projeto — demonstrada ao vivo pelo método `transferSemTratar()` em `Bank.java` e pela seção 2 do `main.rs`.

O único jeito de descartar explicitamente em Rust é `let _ = bank.transfer(...)`, que é **visível e intencional** no código — diferente do `catch` vazio do Java, que parece código correto.

#### Tabela comparativa

| Critério | Java | Rust |
|---|---|---|
| Detecção de erros não tratados | Tempo de execução | Tempo de compilação |
| Erro visível na assinatura | Não (cláusula `throws`) | Sim (tipo de retorno) |
| Catch vazio possível | **Sim** — compilador aceita | **Não** — impossível por design |
| Confiabilidade depende de | Disciplina do programador | Design da linguagem |
| Propagação de erros | Implícita (sobe a pilha) | Explícita (operador `?`) |
| Variantes tipadas do erro | Hierarquia de classes | `enum` com dados (Tipo de Soma) |
| Cobertura de casos | Não verificada | `match` exige cobertura total |
| Erro irrecuperável | `IllegalStateException` (runtime) | `panic!` (aborta o processo) |

### Plano de ensaio da apresentação

- Cada membro apresenta uma parte: introdução, teoria Java, teoria Rust, demo ao vivo, conclusão
- Ensaio completo cronometrado: alvo de 20–25 minutos
- Demo ao vivo: Java no terminal/IDE + Rust no terminal lado a lado
- Ponto de ênfase: executar `Main.java` mostrando a Seção 2 (catch vazio) e o `main.rs` mostrando o `#[must_use]`

---

## SEMANA 6 — Finalização e Apresentação
**23/06/2026 · PENDENTE**

### O que é esperado
- **Entrega:** Relatório Final + Código Final + Slides + Apresentação.
- **Atividade:** conclusão do relatório, polimento do código e ensaio focado na defesa técnica.
- **Objetivo:** preparação para a apresentação no dia 23/06/2026.

### Checklist final

| # | Item | Status |
|---|---|---|
| 1 | Código Java funcionando (5 arquivos `.java`) | CONCLUÍDO |
| 2 | Código Rust funcionando (`domain.rs`, `main.rs`, `stress_tests.rs`) | CONCLUÍDO |
| 3 | Relatório Técnico — `README.md` e `DOCUMENTACAO.md` | CONCLUÍDO |
| 4 | Slides da apresentação (10 slides) | PENDENTE |
| 5 | Guia de perguntas técnicas (10 perguntas) | CONCLUÍDO |
| 6 | Instalar Rust no computador de apresentação | PENDENTE |
| 7 | Testar demo ao vivo (Java + Rust juntos) | PENDENTE |
| 8 | Ensaio completo com todos os membros | PENDENTE |
| 9 | Definir quem apresenta cada parte | PENDENTE |

### Divisão da apresentação por membro

| Membro | Parte da apresentação | Slides |
|---|---|---|
| A definir | Introdução e o problema central | 1, 2, 3 |
| A definir | Fundamentação teórica (Sebesta) | 4 |
| A definir | Java — modelo de exceções | 5 |
| A definir | Rust — modelo `Result<T,E>` | 6 |
| A definir | Comparação, demo ao vivo e conclusão | 7, 8, 9, 10 |

### Critérios de avaliação

| Critério | Peso | O que demonstrar |
|---|---|---|
| Fundamentação Teórica | 2,5 pts | Terminologia correta do Sebesta: exceção, propagação, tipo de soma, confiabilidade, vinculação estática |
| Qualidade da Comparação | 2,5 pts | Vantagens e desvantagens de cada paradigma com exemplos do código |
| Qualidade do Código | 2,0 pts | Código funcionando, seguindo convenções das linguagens |
| Clareza Didática | avaliação dos colegas | Explicar o conceito para quem não conhece a linguagem |
| Postura e Defesa | avaliação dos colegas | Responder bem às perguntas técnicas |

---

## Perguntas técnicas esperadas na defesa

1. Por que `Result<T, E>` é chamado de Tipo de Soma? O que o Sebesta entende por isso?
2. Qual a diferença entre uma exceção verificada e não verificada em Java?
3. O que o atributo `#[must_use]` faz em Rust? Onde ele está definido?
4. Como o operador `?` do Rust se compara ao `throw` do Java?
5. Quando usar `panic!` em vez de `Result`? E `IllegalStateException` em vez de `TransferException`?
6. O `catch` vazio é sempre um problema? Existem casos legítimos?
7. Como o Sebesta define *confiabilidade* de uma linguagem?
8. Por que o sistema de tipos do Rust torna certos bugs impossíveis *por design*?
9. O modelo de Rust tem desvantagens em relação ao Java? Quais?
10. Como o *pattern matching* do Rust garante cobertura total das variantes do erro?
