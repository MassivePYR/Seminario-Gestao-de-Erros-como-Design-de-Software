public class Account {
    public final String id;
    public final String owner;
    public double balance;


    public Account(String id, String owner, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Saldo inicial nao pode ser negativo");
        }
        this.id = id;
        this.owner = owner;
        this.balance = initialBalance;

    }

    @Override
    public String toString() {
        return id + " | " + owner + " | R$" + String.format("%.2f", balance);
    }
}

