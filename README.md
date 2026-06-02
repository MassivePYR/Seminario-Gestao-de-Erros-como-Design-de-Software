# Gestão de Erros como Design de Software

**INF0288 — Linguagens e Paradigmas de Programação (2026.1)**  
**Docente:** Eduardo Simões de Albuquerque  
**Instituto de Informática — Universidade Federal de Goiás**

**Grupo:**
- Aisha Neves de Andrade
- José Jeovah dos Reis Neto
- Marina Pereira Alves
- Rosane Martins de Oliveira
- Viviany Ribeiro Batista

---

## Visão Geral

Este projeto compara dois modelos de gestão de erros aplicados a um sistema de transferências bancárias:

| | Java | Rust |
|---|---|---|
| **Modelo** | Exceções verificadas (`throws` / `try-catch`) | Tipos de Soma (`Result<T, E>`) |
| **Garantia** | Exige declaração ou captura da exceção | Exige consumo do `Result` em tempo de compilação |
| **Erro irrecuperável** | `IllegalStateException` (lançado em runtime) | `panic!` (aborta o processo) |
| **Propagação** | Implícita — sobe a pilha de chamadas | Explícita — operador `?` |
| **Catch vazio possível?** | **Sim** — compilador aceita sem aviso | **Não** — `#[must_use]` rejeita em compilação |
| **Confiabilidade depende de** | Disciplina do programador | Design da linguagem |

### Argumento central

Em Java, um bloco `catch` vazio **compila e executa sem nenhum aviso**. O erro desaparece silenciosamente:

```java
// Java — COMPILA. O erro é engolido. O chamador nunca saberá.
public void transferSemTratar(String from, String to, double amount) {
    try {
        transfer(from, to, amount);
    } catch (TransferException e) {
        // catch vazio — invisível ao compilador
    }
}
```

Em Rust, o equivalente **não compila**:

```rust
// Rust — ERRO DE COMPILAÇÃO:
// error: unused `Result` that must be used
// note: this `Result` may be an `Err` variant, which should be handled
bank.transfer("ACC-001", "ACC-002", 9999.0);
```

O atributo `#[must_use]` no tipo `Result<T, E>` torna **impossível por design** que um erro seja silenciado sem declaração explícita.

---

## Fundamentação Teórica (Sebesta)

| Capítulo | Conceito aplicado |
|---|---|
| **Cap. 14** — Exception Handling | Mecanismo de lançamento, captura e propagação em Java |
| **Cap. 5** — Data Types | Tipos de Soma (`enum Result`) — base teórica do modelo Rust |
| **Cap. 2** — Evolution of Languages | Confiabilidade como propriedade de design de linguagem |
| **Cap. 6** — Scoping & Binding | Vinculação estática — verificação em tempo de compilação |

**Conceito-chave (Sebesta, Cap. 2):** *Confiabilidade* é a propriedade de um programa se comportar conforme sua especificação em todas as condições. Java delega a confiabilidade à disciplina do programador; Rust a incorpora no sistema de tipos.

---

## Estrutura do Projeto

```
Seminario-Gestão-de-Erros-como-Design-de-Software/
│
├── README.md
│
├── java-prototype/                     ← Modelo: Exceções verificadas
│   ├── run.sh                          ← Script de execução
│   └── src/main/java/banking/
│       ├── TransferException.java      ← Hierarquia de exceções tipadas
│       ├── Account.java                ← Domínio: conta bancária
│       ├── Bank.java                   ← transfer() throws + transferSemTratar()
│       ├── StressTests.java            ← 7 cenários de estresse
│       └── Main.java                   ← Ponto de entrada + demonstrações
│
└── rust-prototype/                     ← Modelo: Result<T, E>
    ├── run.sh                          ← Script de execução (requer cargo)
    ├── Cargo.toml
    └── src/
        ├── main.rs                     ← Ponto de entrada + demonstrações
        ├── domain.rs                   ← Account, Bank, TransferError enum
        └── stress_tests.rs             ← 7 cenários de estresse
```

---

## Como Executar

### Java

**Pré-requisito:** Java 21+ (instalar via `brew install openjdk@21`)

```bash
cd java-prototype
./run.sh
```

Ou manualmente:

```bash
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
cd java-prototype
javac -d out src/main/java/banking/*.java
java -cp out banking.Main
```

### Rust

**Pré-requisito:** instalar o Rust/Cargo:

```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
source ~/.cargo/env
```

Depois:

```bash
cd rust-prototype
./run.sh
# ou: cargo run
```

---

## Cenários de Teste Implementados

Os mesmos 7 cenários são implementados em ambas as linguagens para comparação direta:

| # | Cenário | Tipo de erro |
|---|---|---|
| 1 | Saldo insuficiente | Recuperável |
| 2 | Conta inexistente (ACC-999) | Recuperável |
| 3 | Valor negativo | Recuperável |
| 4 | Timeout de rede — sucesso após 2 retries | Recuperável |
| 5 | Timeout de rede — todas as tentativas esgotadas | Recuperável |
| 6 | Transferências sequenciais + conservação de R$1.700,00 | Recuperável |
| 7 | Blast radius — falhas isoladas não contaminam operações seguintes | Recuperável |
| — | Saldo negativo direto na conta (violação de invariante) | **Irrecuperável** (`panic!` / `IllegalStateException`) |

---

## Detalhes de Implementação

### Java — `TransferException.java`

Hierarquia de exceções verificadas. Cada tipo de falha é uma subclasse com campos tipados:

```java
// Exceção base — checked: o compilador exige declaração ou captura
public class TransferException extends Exception {
    public static class InsufficientFunds extends TransferException {
        public final double available;
        public final double requested;
        // ...
    }
    public static class AccountNotFound extends TransferException { ... }
    public static class NegativeAmount   extends TransferException { ... }
    public static class NetworkTimeout   extends TransferException { ... }
}
```

O método `transferSemTratar()` em `Bank.java` é a **demonstração central**: mostra que o compilador Java aceita um `catch` completamente vazio sem emitir nenhum aviso. A mesma falha que derrubaria um sistema em produção passa invisível.

### Rust — `domain.rs`

Todos os erros são variantes de um único `enum` — o Tipo de Soma do Sebesta:

```rust
#[derive(Debug, PartialEq)]
pub enum TransferError {
    InsufficientFunds { available: f64, requested: f64 },
    AccountNotFound(String),
    NegativeAmount(f64),
    NetworkTimeout { retries: u32 },
    InvalidLedgerState(String),
}

// #[must_use] é herdado do tipo Result<T, E> da stdlib
// O compilador rejeita qualquer chamada que ignore o retorno
#[must_use]
pub fn transfer(&mut self, from_id: &str, to_id: &str, amount: f64)
    -> Result<f64, TransferError> { ... }
```

O operador `?` propaga erros com zero boilerplate, mantendo o tipo visível na assinatura da função.

### Erros irrecuperáveis: `panic!` vs `IllegalStateException`

Ambas as linguagens distinguem erros recuperáveis (tratáveis) de irrecuperáveis (violações de invariante):

```rust
// Rust: panic! aborta o processo — equivalente a corrupção de estado
fn assert_ledger_integrity(balance: f64, account_id: &str) {
    if balance < 0.0 {
        panic!("VIOLAÇÃO DE INVARIANTE: conta '{}' com saldo {:.2}", account_id, balance);
    }
}
```

```java
// Java: IllegalStateException lançada como unchecked — não exige declaração
void debit(double amount) {
    balance -= amount;
    if (balance < 0) {
        throw new IllegalStateException("VIOLAÇÃO DE INVARIANTE: " + id);
    }
}
```

---

## Tabela Comparativa Completa

| Critério | Java | Rust |
|---|---|---|
| Detecção de erros não tratados | Tempo de execução | Tempo de compilação |
| Erro visível na assinatura | Parcial (`throws` separado do retorno) | Sim — faz parte de `Result<T, E>` |
| Catch vazio possível | **Sim** — compilador aceita | **Não** — impossível por design |
| Confiabilidade depende de | Disciplina do programador | Design da linguagem |
| Propagação de erros | Implícita — sobe a pilha | Explícita — operador `?` |
| Variantes tipadas do erro | Hierarquia de classes | `enum` com dados (Tipo de Soma) |
| Cobertura de casos garantida | Não verificada | `match` exige cobertura total |
| Erro irrecuperável | `IllegalStateException` (runtime) | `panic!` (aborta o processo) |

---

## Referências

- SEBESTA, Robert W. *Concepts of Programming Languages*. 12ª ed. Pearson, 2019.
  - Cap. 2 — Evolution of Languages (confiabilidade)
  - Cap. 5 — Data Types (Tipos de Soma, enums)
  - Cap. 6 — Names, Bindings, and Scopes (vinculação estática)
  - Cap. 14 — Exception Handling and Event Handling
- [The Rust Reference — `#[must_use]`](https://doc.rust-lang.org/reference/attributes/diagnostics.html#the-must_use-attribute)
- [Java Language Specification — Checked Exceptions](https://docs.oracle.com/javase/specs/)
