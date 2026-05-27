import Foundation

public class Account {
    public let id: String
    public let owner: String
    public private(set) var balance: Double

    public init(id: String, owner: String, initialBalance: Double) {
        precondition(initialBalance >= 0, "Saldo inicial não pode ser negativo")
        self.id = id
        self.owner = owner
        self.balance = initialBalance
    }

    fileprivate func debit(_ amount: Double) {
        balance -= amount
        // Erro irrecuperável: fatalError — equivalente ao panic! do Rust
        guard balance >= 0 else {
            fatalError("VIOLAÇÃO DE INVARIANTE: conta '\(id)' com saldo negativo (\(balance)). Estado corrompido.")
        }
    }

    fileprivate func credit(_ amount: Double) {
        balance += amount
    }
}

public class Bank {
    private var accounts: [String: Account] = [:]
    public private(set) var transactionLog: [String] = []

    public init() {}

    public func addAccount(_ account: Account) {
        accounts[account.id] = account
    }

    public func balance(of id: String) -> Double? {
        accounts[id]?.balance
    }

    // throws declara explicitamente que esta função pode falhar
    // O compilador exige try na chamada — análogo ao #[must_use] do Rust
    public func transfer(from fromId: String, to toId: String, amount: Double) throws -> Double {
        guard amount > 0 else {
            throw TransferError.negativeAmount(amount)
        }

        guard let fromAccount = accounts[fromId] else {
            throw TransferError.accountNotFound(id: fromId)
        }

        guard let toAccount = accounts[toId] else {
            throw TransferError.accountNotFound(id: toId)
        }

        guard amount <= fromAccount.balance else {
            throw TransferError.insufficientFunds(
                available: fromAccount.balance,
                requested: amount
            )
        }

        fromAccount.debit(amount)
        toAccount.credit(amount)

        let entry = String(format: "[OK] %@ → %@: R$%.2f (saldo restante: R$%.2f)",
                          fromId, toId, amount, fromAccount.balance)
        transactionLog.append(entry)

        return fromAccount.balance
    }

    // Retry com propagação de erro tipada
    public func transfer(
        from fromId: String,
        to toId: String,
        amount: Double,
        maxRetries: Int,
        simulateFailures: Int
    ) throws -> Double {
        for attempt in 0..<maxRetries {
            if attempt < simulateFailures {
                print("  [tentativa \(attempt + 1)/\(maxRetries)] Simulando timeout de rede...")
                Thread.sleep(forTimeInterval: 0.1)
                continue
            }
            return try transfer(from: fromId, to: toId, amount: amount)
        }
        throw TransferError.networkTimeout(retries: maxRetries)
    }
}
