public abstract class TransferError extends Exception {
    public TransferError(String msg) {
        super(msg);
    }

    //saldo insuficiente
    public static class InsufficientFunds extends TransferError {
        public final double available;
        public final double requested;

        public InsufficientFunds( double available, double requested) {
            super("Saldo insuficiente: tem R$" + String.format("%.2f", available) + " mas tentou R$" + String.format("%.2f", requested));
            this.available = available;
            this.requested = requested;
        }
    }

    //conta inexistente no sistema
    public static class AccountNotFound extends TransferError {
        public final String accountId;

        public AccountNotFound(String id) {
            super("Conta nao encontrada: " + id);
            this.accountId = id;

        }
    }

    //valores invalidos - negativo, zero
    public static class NegativeAmount extends TransferError {
        public final double amount;
        public NegativeAmount(double amount) {
            super("Valor invalido: " + String.format("%.2f", amount));
            this.amount = amount;
        }
    }

    // timeout de rede apos N tentativas
    public static class NetworkTimeout extends TransferError {
        public final int retries;

        public NetworkTimeout(int retries){
            super("Timeout apos " + retries + " tentativas");
            this.retries = retries;
        }
    }

    // erros irrecuperaveis - em rust seria panic!
    public static class InvalidLedgerState extends RuntimeException {
        public InvalidLedgerState(String msg) {
            super("Estado invalido: " + msg);
        }
    }
}
