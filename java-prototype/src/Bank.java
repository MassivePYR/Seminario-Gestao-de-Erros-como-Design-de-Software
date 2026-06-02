import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class Bank {
    private List<Account> accounts = new ArrayList<>();
    public List<String> transactionLog = new ArrayList<>();

    public void addAccount(Account acc) {
        accounts.add(acc);
    }

    private Optional<Integer> findAccount(String id) {
        for (int i = 0; i < accounts.size(); i++){
            if (accounts.get(i).id.equals(id)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }
    private void checkBalance(double balance, String accountId) {
        if (balance < 0) {
            throw new TransferError.InvalidLedgerState(
                    "conta " + accountId + "ficou com saldo negativo: " + balance
            );
        }
    }

    public double transfer(String fromId, String toId, double amount)
        throws TransferError {

        if(amount <= 0) {
            throw new TransferError.NegativeAmount(amount);
        }

        int fromIdx = findAccount(fromId)
                .orElseThrow(() -> new TransferError.AccountNotFound(fromId));
        int toIdx = findAccount(toId)
                .orElseThrow(() -> new TransferError.AccountNotFound(toId));
        double saldoAtual = accounts.get(fromIdx).balance;

        if (amount > saldoAtual) {
            throw new TransferError.InsufficientFunds(saldoAtual, amount);
        }

        accounts.get(fromIdx).balance -= amount;
        accounts.get(toIdx).balance += amount;

        checkBalance(accounts.get(fromIdx).balance, fromId);
        checkBalance(accounts.get(toIdx).balance, toId);

        String log = "[OK] " + fromId + " -> " + toId + ": R$"
                + String.format("%.2f" , amount) + "(saldo: R$"
                + String.format("%.2f", accounts.get(fromIdx).balance) + ")";
        transactionLog.add(log);

        return accounts.get(fromIdx).balance;
    }

    public double transferWithRetry(String fromId, String toId, double amount,
                                    int maxRetries, int simulateFailures)
        throws TransferError {
        for (int i = 0; i < maxRetries; i++) {
            if (i < simulateFailures) {
                System.out.println(" [tentativa " + (i + 1) + "/" + maxRetries + "] timeout de rede..." );
                try { Thread.sleep(100); }
                catch (InterruptedException e) {

                }
                continue;
            }
            return transfer(fromId, toId, amount);
        }
        throw new TransferError.NetworkTimeout(maxRetries);
    }

    // transferir sem tratar
    public void transferSemTratar(String fromId, String toId, double amount) {
        try {
            transfer(fromId, toId, amount);
        } catch (TransferError e) {
            // VAZIO - Ignorando erro - compila e executa sem alerta
        }
        System.out.println(" [catch vazio] programa continua mesmo com erro");
    }

    public Optional<Double> balanceOf(String id) {
        return findAccount(id).map(i -> accounts.get(i).balance);
    }
}
