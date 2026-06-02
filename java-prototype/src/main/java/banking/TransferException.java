package banking;

// Exceção base — checked exception: o compilador exige tratamento explícito
public class TransferException extends Exception {

    public TransferException(String message) {
        super(message);
    }

    // Subclasses tipadas — permitem catch por tipo específico
    public static class InsufficientFunds extends TransferException {
        public final double available;
        public final double requested;

        public InsufficientFunds(double available, double requested) {
            super(String.format(
                "Saldo insuficiente: disponível R$%.2f, solicitado R$%.2f",
                available, requested));
            this.available = available;
            this.requested = requested;
        }
    }

    public static class AccountNotFound extends TransferException {
        public final String accountId;

        public AccountNotFound(String accountId) {
            super("Conta não encontrada: " + accountId);
            this.accountId = accountId;
        }
    }

    public static class NegativeAmount extends TransferException {
        public final double amount;

        public NegativeAmount(double amount) {
            super(String.format("Valor inválido: R$%.2f (deve ser positivo)", amount));
            this.amount = amount;
        }
    }

    public static class NetworkTimeout extends TransferException {
        public final int retries;

        public NetworkTimeout(int retries) {
            super("Timeout de rede após " + retries + " tentativas");
            this.retries = retries;
        }
    }
}
