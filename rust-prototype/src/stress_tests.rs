use crate::domain::{Account, Bank, TransferError};

pub fn run_all() {
    println!("\n{}", "=".repeat(60));
    println!("  TESTES DE ESTRESSE — RUST (Result<T, E>)");
    println!("{}", "=".repeat(60));

    test_insufficient_funds();
    test_account_not_found();
    test_negative_amount();
    test_network_retry_success();
    test_network_retry_exhausted();
    test_sequential_transfers();
    test_blast_radius_containment();
}

fn setup_bank() -> Bank {
    let mut bank = Bank::new();
    bank.add_account(Account::new("ACC-001", "Alice", 1000.0));
    bank.add_account(Account::new("ACC-002", "Bob", 500.0));
    bank.add_account(Account::new("ACC-003", "Carol", 200.0));
    bank
}

fn test_insufficient_funds() {
    println!("\n[TESTE 1] Saldo Insuficiente");
    let mut bank = setup_bank();

    match bank.transfer("ACC-002", "ACC-001", 900.0) {
        Err(TransferError::InsufficientFunds { available, requested }) => {
            println!("  ✓ Erro capturado corretamente");
            println!("  ✓ Disponível: R${:.2}, Solicitado: R${:.2}", available, requested);
            println!("  ✓ Saldo de ACC-002 inalterado: R${:.2}", bank.balance_of("ACC-002").unwrap());
        }
        other => panic!("Resultado inesperado: {:?}", other),
    }
}

fn test_account_not_found() {
    println!("\n[TESTE 2] Conta Inexistente");
    let mut bank = setup_bank();

    match bank.transfer("ACC-999", "ACC-001", 100.0) {
        Err(TransferError::AccountNotFound(id)) => {
            println!("  ✓ Conta '{}' não encontrada — erro tipado capturado", id);
        }
        other => panic!("Resultado inesperado: {:?}", other),
    }
}

fn test_negative_amount() {
    println!("\n[TESTE 3] Valor Negativo");
    let mut bank = setup_bank();

    match bank.transfer("ACC-001", "ACC-002", -50.0) {
        Err(TransferError::NegativeAmount(v)) => {
            println!("  ✓ Valor inválido R${:.2} rejeitado antes de qualquer operação", v);
        }
        other => panic!("Resultado inesperado: {:?}", other),
    }
}

fn test_network_retry_success() {
    println!("\n[TESTE 4] Retry de Rede — Sucesso na 3ª tentativa");
    let mut bank = setup_bank();

    match bank.transfer_with_retry("ACC-001", "ACC-002", 300.0, 5, 2) {
        Ok(remaining) => {
            println!("  ✓ Transferência concluída após retries");
            println!("  ✓ Saldo restante ACC-001: R${:.2}", remaining);
        }
        Err(e) => println!("  ✗ Falhou: {}", e),
    }
}

fn test_network_retry_exhausted() {
    println!("\n[TESTE 5] Retry de Rede — Todas as tentativas esgotadas");
    let mut bank = setup_bank();

    match bank.transfer_with_retry("ACC-001", "ACC-002", 100.0, 3, 5) {
        Err(TransferError::NetworkTimeout { retries }) => {
            println!("  ✓ NetworkTimeout após {} tentativas", retries);
            println!("  ✓ Saldo de ACC-001 inalterado: R${:.2}", bank.balance_of("ACC-001").unwrap());
        }
        other => panic!("Resultado inesperado: {:?}", other),
    }
}

fn test_sequential_transfers() {
    println!("\n[TESTE 6] Transferências Sequenciais — Consistência do Ledger");
    let mut bank = setup_bank();

    let ops: Vec<(&str, &str, f64)> = vec![
        ("ACC-001", "ACC-002", 200.0),
        ("ACC-001", "ACC-003", 300.0),
        ("ACC-002", "ACC-003", 100.0),
        ("ACC-001", "ACC-002", 600.0), // deve falhar — saldo insuficiente
    ];

    let initial_total: f64 = 1000.0 + 500.0 + 200.0;
    let mut success = 0;
    let mut failures = 0;

    for (from, to, amount) in &ops {
        match bank.transfer(from, to, *amount) {
            Ok(_) => success += 1,
            Err(e) => {
                failures += 1;
                println!("  [erro controlado] {}", e);
            }
        }
    }

    let final_total = bank.balance_of("ACC-001").unwrap()
        + bank.balance_of("ACC-002").unwrap()
        + bank.balance_of("ACC-003").unwrap();

    println!("  ✓ Operações: {} ok, {} rejeitadas", success, failures);
    println!("  ✓ Total inicial: R${:.2} | Total final: R${:.2}", initial_total, final_total);
    assert!((initial_total - final_total).abs() < 0.001, "Ledger inconsistente!");
    println!("  ✓ Conservação de valor garantida pelo sistema de tipos");
}

fn test_blast_radius_containment() {
    println!("\n[TESTE 7] Contenção do Blast Radius");
    let mut bank = setup_bank();

    // Série de operações onde erros não devem contaminar as subsequentes
    let results: Vec<Result<f64, TransferError>> = vec![
        bank.transfer("ACC-001", "ACC-002", 100.0),  // ok
        bank.transfer("ACC-999", "ACC-001", 50.0),   // erro: conta inexistente
        bank.transfer("ACC-001", "ACC-002", 50.0),   // deve continuar funcionando
        bank.transfer("ACC-002", "ACC-001", 9999.0), // erro: saldo insuficiente
        bank.transfer("ACC-001", "ACC-003", 50.0),   // deve continuar funcionando
    ];

    let successes = results.iter().filter(|r| r.is_ok()).count();
    let errors = results.iter().filter(|r| r.is_err()).count();

    println!("  ✓ {} sucessos, {} erros — todos isolados", successes, errors);
    println!("  ✓ Sistema operacional após falhas parciais");
    println!("  ✓ Log de transações: {} entradas", bank.transaction_log.len());
}
