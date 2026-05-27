mod domain;
mod stress_tests;

use domain::{Account, Bank};

fn main() {
    println!("╔══════════════════════════════════════════════════════════╗");
    println!("║  PROTÓTIPO ALPHA — Gestão de Erros como Design           ║");
    println!("║  Linguagem: Rust  |  Modelo: Result<T, E>                ║");
    println!("╚══════════════════════════════════════════════════════════╝");

    println!("\n--- DEMONSTRAÇÃO BÁSICA ---");

    let mut bank = Bank::new();
    bank.add_account(Account::new("ACC-001", "Alice", 1000.0));
    bank.add_account(Account::new("ACC-002", "Bob", 500.0));

    println!("Contas criadas: Alice R$1000.00, Bob R$500.00");

    // O compilador exige que Result seja tratado (#[must_use])
    match bank.transfer("ACC-001", "ACC-002", 300.0) {
        Ok(saldo) => println!("Transferência ok. Saldo Alice: R${:.2}", saldo),
        Err(e) => println!("Erro: {}", e),
    }

    match bank.transfer("ACC-002", "ACC-001", 9000.0) {
        Ok(_) => println!("Transferência ok"),
        Err(e) => println!("Erro tratado: {}", e),
    }

    stress_tests::run_all();

    println!("\n{}", "=".repeat(60));
    println!("  RESUMO: Todos os erros recuperáveis foram tratados via");
    println!("  Result<T, E>. Nenhuma exceção não tratada escapou.");
    println!("{}", "=".repeat(60));
}
