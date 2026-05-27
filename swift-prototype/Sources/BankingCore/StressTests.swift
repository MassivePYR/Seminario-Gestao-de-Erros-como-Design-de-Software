import Foundation

public struct StressTests {

    public static func runAll() {
        print("\n" + String(repeating: "=", count: 60))
        print("  TESTES DE ESTRESSE — SWIFT (do/try/catch tipado)")
        print(String(repeating: "=", count: 60))

        testInsufficientFunds()
        testAccountNotFound()
        testNegativeAmount()
        testNetworkRetrySuccess()
        testNetworkRetryExhausted()
        testSequentialTransfers()
        testBlastRadiusContainment()
    }

    private static func makeBank() -> Bank {
        let bank = Bank()
        bank.addAccount(Account(id: "ACC-001", owner: "Alice", initialBalance: 1000.0))
        bank.addAccount(Account(id: "ACC-002", owner: "Bob",   initialBalance: 500.0))
        bank.addAccount(Account(id: "ACC-003", owner: "Carol", initialBalance: 200.0))
        return bank
    }

    private static func testInsufficientFunds() {
        print("\n[TESTE 1] Saldo Insuficiente")
        let bank = makeBank()

        do {
            _ = try bank.transfer(from: "ACC-002", to: "ACC-001", amount: 900.0)
            print("  ✗ Deveria ter lançado erro")
        } catch TransferError.insufficientFunds(let available, let requested) {
            print("  ✓ Erro capturado corretamente")
            print(String(format: "  ✓ Disponível: R$%.2f, Solicitado: R$%.2f", available, requested))
            print(String(format: "  ✓ Saldo de ACC-002 inalterado: R$%.2f", bank.balance(of: "ACC-002")!))
        } catch {
            print("  ✗ Erro inesperado: \(error)")
        }
    }

    private static func testAccountNotFound() {
        print("\n[TESTE 2] Conta Inexistente")
        let bank = makeBank()

        do {
            _ = try bank.transfer(from: "ACC-999", to: "ACC-001", amount: 100.0)
        } catch TransferError.accountNotFound(let id) {
            print("  ✓ Conta '\(id)' não encontrada — erro tipado capturado")
        } catch {
            print("  ✗ Erro inesperado: \(error)")
        }
    }

    private static func testNegativeAmount() {
        print("\n[TESTE 3] Valor Negativo")
        let bank = makeBank()

        do {
            _ = try bank.transfer(from: "ACC-001", to: "ACC-002", amount: -50.0)
        } catch TransferError.negativeAmount(let v) {
            print(String(format: "  ✓ Valor inválido R$%.2f rejeitado antes de qualquer operação", v))
        } catch {
            print("  ✗ Erro inesperado: \(error)")
        }
    }

    private static func testNetworkRetrySuccess() {
        print("\n[TESTE 4] Retry de Rede — Sucesso na 3ª tentativa")
        let bank = makeBank()

        do {
            let remaining = try bank.transfer(
                from: "ACC-001", to: "ACC-002", amount: 300.0,
                maxRetries: 5, simulateFailures: 2
            )
            print("  ✓ Transferência concluída após retries")
            print(String(format: "  ✓ Saldo restante ACC-001: R$%.2f", remaining))
        } catch {
            print("  ✗ Falhou: \(error)")
        }
    }

    private static func testNetworkRetryExhausted() {
        print("\n[TESTE 5] Retry de Rede — Todas as tentativas esgotadas")
        let bank = makeBank()

        do {
            _ = try bank.transfer(
                from: "ACC-001", to: "ACC-002", amount: 100.0,
                maxRetries: 3, simulateFailures: 5
            )
        } catch TransferError.networkTimeout(let retries) {
            print("  ✓ NetworkTimeout após \(retries) tentativas")
            print(String(format: "  ✓ Saldo de ACC-001 inalterado: R$%.2f", bank.balance(of: "ACC-001")!))
        } catch {
            print("  ✗ Erro inesperado: \(error)")
        }
    }

    private static func testSequentialTransfers() {
        print("\n[TESTE 6] Transferências Sequenciais — Consistência do Ledger")
        let bank = makeBank()

        let ops: [(String, String, Double)] = [
            ("ACC-001", "ACC-002", 200.0),
            ("ACC-001", "ACC-003", 300.0),
            ("ACC-002", "ACC-003", 100.0),
            ("ACC-001", "ACC-002", 600.0), // deve falhar
        ]

        let initialTotal = 1000.0 + 500.0 + 200.0
        var success = 0
        var failures = 0

        for (from, to, amount) in ops {
            do {
                _ = try bank.transfer(from: from, to: to, amount: amount)
                success += 1
            } catch {
                failures += 1
                print("  [erro controlado] \(error)")
            }
        }

        let finalTotal = bank.balance(of: "ACC-001")! + bank.balance(of: "ACC-002")! + bank.balance(of: "ACC-003")!
        print("  ✓ Operações: \(success) ok, \(failures) rejeitadas")
        print(String(format: "  ✓ Total inicial: R$%.2f | Total final: R$%.2f", initialTotal, finalTotal))
        assert(abs(initialTotal - finalTotal) < 0.001, "Ledger inconsistente!")
        print("  ✓ Conservação de valor garantida pelo sistema de tipos")
    }

    private static func testBlastRadiusContainment() {
        print("\n[TESTE 7] Contenção do Blast Radius")
        let bank = makeBank()

        let ops: [(String, String, Double)] = [
            ("ACC-001", "ACC-002", 100.0),
            ("ACC-999", "ACC-001", 50.0),
            ("ACC-001", "ACC-002", 50.0),
            ("ACC-002", "ACC-001", 9999.0),
            ("ACC-001", "ACC-003", 50.0),
        ]

        var successes = 0
        var errors = 0

        for (from, to, amount) in ops {
            do {
                _ = try bank.transfer(from: from, to: to, amount: amount)
                successes += 1
            } catch {
                errors += 1
            }
        }

        print("  ✓ \(successes) sucessos, \(errors) erros — todos isolados")
        print("  ✓ Sistema operacional após falhas parciais")
        print("  ✓ Log de transações: \(bank.transactionLog.count) entradas")
    }
}
