mod domain;
mod stress_tests;

use domain::{Account, Bank};

fn main() {
    println!("╔══════════════════════════════════════════════════════════╗");
    println!("║  PROTÓTIPO FINAL — Gestão de Erros como Design           ║");
    println!("║  Linguagem: Rust  |  Modelo: Result<T, E>                ║");
    println!("║  INF0288 LPP — UFG / INF — Junho de 2026                 ║");
    println!("╚══════════════════════════════════════════════════════════╝");

    demonstracao_basica();
    demonstracao_must_use();
    stress_tests::run_all();
    resumo();
}

// ─── 1. Fluxo normal com tratamento explícito ────────────────────────
fn demonstracao_basica() {
    println!("\n{}", "─".repeat(60));
    println!("  SEÇÃO 1: Fluxo normal com tratamento explícito");
    println!("{}", "─".repeat(60));

    let mut bank = Bank::new();
    bank.add_account(Account::new("ACC-001", "Alice", 1000.0));
    bank.add_account(Account::new("ACC-002", "Bob", 500.0));
    println!("Contas: Alice R$1000,00 | Bob R$500,00\n");

    // #[must_use] — o compilador exige que o Result seja consumido
    match bank.transfer("ACC-001", "ACC-002", 300.0) {
        Ok(saldo) => println!("  [OK] Alice → Bob R$300,00. Saldo Alice: R${:.2}", saldo),
        Err(e)    => println!("  [ERRO] {}", e),
    }

    match bank.transfer("ACC-002", "ACC-001", 9000.0) {
        Ok(_)  => println!("  [OK] Transferência realizada"),
        Err(e) => {
            println!("  [ERRO TRATADO] {}", e);
            println!("  Saldo de Bob inalterado: R${:.2}", bank.balance_of("ACC-002").unwrap());
        }
    }
}

// ─── 2. Demonstração do #[must_use] — contraste com catch vazio ──────
fn demonstracao_must_use() {
    println!("\n{}", "─".repeat(60));
    println!("  SEÇÃO 2: #[must_use] — a impossibilidade do erro silencioso");
    println!("{}", "─".repeat(60));
    println!("  Em Rust, Result<T,E> carrega #[must_use].");
    println!("  Ignorar o valor causa ERRO DE COMPILAÇÃO — não aviso em runtime.\n");

    println!("  O que Java permite (catch vazio — compila sem aviso):");
    println!("  ┌─────────────────────────────────────────────────────┐");
    println!("  │  void transferSemTratar(from, to, amount) {{          │");
    println!("  │      try {{ transfer(from, to, amount); }}             │");
    println!("  │      catch (TransferException e) {{ /* vazio */ }}     │");
    println!("  │  }}  // compilador Java: sem aviso. Erro engolido.    │");
    println!("  └─────────────────────────────────────────────────────┘\n");

    println!("  O que Rust impede (não compila):");
    println!("  ┌─────────────────────────────────────────────────────┐");
    println!("  │  bank.transfer(\"A\", \"B\", 9999.0);                   │");
    println!("  │  // error[E0282]: unused `Result` that must be used  │");
    println!("  │  // #[must_use] aplicado ao tipo Result<T,E>         │");
    println!("  └─────────────────────────────────────────────────────┘\n");

    println!("  A única forma de descartar explicitamente em Rust:");

    let mut bank = Bank::new();
    bank.add_account(Account::new("ACC-001", "Alice", 500.0));
    bank.add_account(Account::new("ACC-002", "Bob", 500.0));

    let saldo_antes = bank.balance_of("ACC-001").unwrap();

    // `let _ = ...` é o único jeito de ignorar — é INTENCIONAL e VISÍVEL no código.
    // Diferente do catch vazio Java, aqui o descarte é declarado explicitamente.
    let _ = bank.transfer("ACC-001", "ACC-002", 9999.0);

    let saldo_depois = bank.balance_of("ACC-001").unwrap();

    println!("  let _ = bank.transfer(...); // descarte EXPLÍCITO e visível");
    println!("  Tentativa de R$9999,00 com saldo R${:.2}", saldo_antes);
    println!("  Saldo após: R${:.2} — inalterado (falha foi para o `_`)", saldo_depois);
    println!();
    println!("  DIFERENÇA FUNDAMENTAL:");
    println!("  Java: catch vazio é invisível — parece código correto.");
    println!("  Rust: let _ = ...  é visível — o revisor vê o descarte.");
}

// ─── 3. Resumo final ─────────────────────────────────────────────────
fn resumo() {
    println!("\n{}", "=".repeat(60));
    println!("  CONCLUSÃO");
    println!("{}", "=".repeat(60));
    println!("  Java:  confiabilidade depende da DISCIPLINA do programador.");
    println!("         Um catch vazio compila e executa sem aviso algum.");
    println!();
    println!("  Rust:  confiabilidade é garantida pelo DESIGN da linguagem.");
    println!("         Result<T,E> com #[must_use] torna erros silenciosos");
    println!("         impossíveis por construção — não por convenção.");
    println!("{}", "=".repeat(60));
}
