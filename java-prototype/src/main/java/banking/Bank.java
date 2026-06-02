package banking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bank {
    private final Map<String, Account> accounts = new HashMap<>();
    private final List<String> transactionLog = new ArrayList<>();

    public void addAccount(Account account) {
        accounts.put(account.id, account);
    }

    public Account getAccount(String id) {
        return accounts.get(id);
    }

    public List<String> getTransactionLog() {
        return List.copyOf(transactionLog);
    }

    // throws TransferException — o compilador exige que o chamador trate ou declare
    // Contraste com Rust: lá o compilador exige via #[must_use] no Result<T, E>
    public double transfer(String fromId, String toId, double amount)
            throws TransferException {

        if (amount <= 0) {
            throw new TransferException.NegativeAmount(amount);
        }

        Account from = accounts.get(fromId);
        if (from == null) {
            throw new TransferException.AccountNotFound(fromId);
        }

        Account to = accounts.get(toId);
        if (to == null) {
            throw new TransferException.AccountNotFound(toId);
        }

        if (amount > from.getBalance()) {
            throw new TransferException.InsufficientFunds(from.getBalance(), amount);
        }

        from.debit(amount);
        to.credit(amount);

        String entry = String.format("[OK] %s → %s: R$%.2f (saldo restante: R$%.2f)",
                fromId, toId, amount, from.getBalance());
        transactionLog.add(entry);

        return from.getBalance();
    }

    // ─────────────────────────────────────────────────────────────────
    // DEMONSTRAÇÃO CENTRAL DO SEMINÁRIO: o catch vazio
    //
    // Este método faz a mesma transferência que transfer(), mas captura
    // a exceção em um bloco vazio. O compilador Java ACEITA este código
    // sem qualquer aviso. A falha desaparece silenciosamente.
    //
    // Em Rust, o equivalente é IMPOSSÍVEL por design:
    //   bank.transfer("A", "B", 100.0);  // erro de compilação — Result ignorado
    // O atributo #[must_use] faz o compilador rejeitar código que descarta Result.
    // ─────────────────────────────────────────────────────────────────
    public void transferSemTratar(String fromId, String toId, double amount) {
        try {
            transfer(fromId, toId, amount);
        } catch (TransferException e) {
            // catch vazio — a falha é engolida, o chamador nunca saberá
            // o saldo permanece inalterado, mas nenhum sinal é emitido
        }
    }

    // Retry com re-lançamento tipado — mantém o tipo do erro original
    public double transferWithRetry(String fromId, String toId, double amount,
                                    int maxRetries, int simulateFailures)
            throws TransferException {

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            if (attempt < simulateFailures) {
                System.out.printf("  [tentativa %d/%d] Simulando timeout de rede...%n",
                        attempt + 1, maxRetries);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }
            return transfer(fromId, toId, amount);
        }
        throw new TransferException.NetworkTimeout(maxRetries);
    }
}
