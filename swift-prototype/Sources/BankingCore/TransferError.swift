// Protocolo de erro tipado — equivalente ao enum TransferError do Rust
public enum TransferError: Error, CustomStringConvertible {
    case insufficientFunds(available: Double, requested: Double)
    case accountNotFound(id: String)
    case negativeAmount(Double)
    case networkTimeout(retries: Int)
    case invalidLedgerState(String)

    public var description: String {
        switch self {
        case .insufficientFunds(let available, let requested):
            return String(format: "Saldo insuficiente: disponível R$%.2f, solicitado R$%.2f", available, requested)
        case .accountNotFound(let id):
            return "Conta não encontrada: \(id)"
        case .negativeAmount(let amount):
            return String(format: "Valor inválido: R$%.2f (deve ser positivo)", amount)
        case .networkTimeout(let retries):
            return "Timeout de rede após \(retries) tentativas"
        case .invalidLedgerState(let msg):
            return "Estado inválido do ledger: \(msg)"
        }
    }
}
