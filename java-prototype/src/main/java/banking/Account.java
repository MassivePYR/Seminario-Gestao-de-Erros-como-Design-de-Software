package banking;

public class Account {
    public final String id;
    public final String owner;
    private double balance;

    public Account(String id, String owner, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Saldo inicial não pode ser negativo");
        }
        this.id = id;
        this.owner = owner;
        this.balance = initialBalance;
    }

    public double getBalance() {
        return balance;
    }

    void debit(double amount) {
        balance -= amount;
        // Erro irrecuperável: viola invariante do sistema — equivalente ao panic! do Rust
        if (balance < 0) {
            throw new IllegalStateException(
                String.format("VIOLAÇÃO DE INVARIANTE: conta '%s' com saldo negativo (%.2f).", id, balance));
        }
    }

    void credit(double amount) {
        balance += amount;
    }

    @Override
    public String toString() {
        return String.format("Account[%s, %s, R$%.2f]", id, owner, balance);
    }
}
