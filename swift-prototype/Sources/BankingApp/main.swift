import BankingCore
import Foundation

print("╔══════════════════════════════════════════════════════════╗")
print("║  PROTÓTIPO ALPHA — Gestão de Erros como Design           ║")
print("║  Linguagem: Swift  |  Modelo: do/try/catch tipado        ║")
print("╚══════════════════════════════════════════════════════════╝")

print("\n--- DEMONSTRAÇÃO BÁSICA ---")

let bank = Bank()
bank.addAccount(Account(id: "ACC-001", owner: "Alice", initialBalance: 1000.0))
bank.addAccount(Account(id: "ACC-002", owner: "Bob", initialBalance: 500.0))

print("Contas criadas: Alice R$1000.00, Bob R$500.00")

// Swift exige 'try' explícito — o compilador não deixa ignorar o erro
do {
    let saldo = try bank.transfer(from: "ACC-001", to: "ACC-002", amount: 300.0)
    print(String(format: "Transferência ok. Saldo Alice: R$%.2f", saldo))
} catch {
    print("Erro: \(error)")
}

do {
    _ = try bank.transfer(from: "ACC-002", to: "ACC-001", amount: 9000.0)
} catch {
    print("Erro tratado: \(error)")
}

StressTests.runAll()

print("\n" + String(repeating: "=", count: 60))
print("  RESUMO: Todos os erros recuperáveis foram tratados via")
print("  do/try/catch tipado. Nenhuma exceção não tratada escapou.")
print(String(repeating: "=", count: 60))
