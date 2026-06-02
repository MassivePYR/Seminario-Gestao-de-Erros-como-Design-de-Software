public class Main {

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("  Gestao de Erros como Design de Software - Sistema Bancario");
        System.out.println("=".repeat(70));

        Bank bank = new Bank();
        bank.addAccount(new Account("ACC-001", "Alice", 1000.0));
        bank.addAccount(new Account("ACC-002", "Bob", 500.0));

        System.out.println("contas: Alice R$1000.00, Bob R$500.00");

// transferencia que funciona
        try {
            double saldo = bank.transfer("ACC-001", "ACC-002", 300.0);
            System.out.printf("transferencia ok. saldo Alice: R$%.2f%n", saldo);
        } catch (TransferError e) {
            System.out.println("erro: " + e.getMessage());
        }

// transferencia que falha
        try {
            bank.transfer("ACC-002", "ACC-001", 9000.0);
        } catch (TransferError e) {
            System.out.println("erro tratado: " + e.getMessage());
        }

// roda os 7 testes
        StressTests.runAll();

// demonstra o catch vazio
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  CATCH VAZIO - impossivel em Rust");
        System.out.println("=".repeat(70));

        Bank bank2 = new Bank();
        bank2.addAccount(new Account("ACC-001", "Alice", 1000.0));
        bank2.addAccount(new Account("ACC-002", "Bob", 500.0));
        bank2.addAccount(new Account("ACC-003", "Carol", 200.0));

        System.out.println("\n[1] saldo insuficiente - sem tratamento:");
        bank2.transferSemTratar("ACC-002", "ACC-001", 900.0);
        System.out.printf("  saldo ACC-002: R$%.2f (nao mudou, mas nao avisou nada)%n",
                bank2.balanceOf("ACC-002").orElse(0.0));

        System.out.println("\n[2] conta inexistente - sem tratamento:");
        bank2.transferSemTratar("ACC-999", "ACC-001", 100.0);

        System.out.println("\n[3] valor negativo - sem tratamento:");
        bank2.transferSemTratar("ACC-001", "ACC-002", -999.0);

        System.out.println("\nconclusao: java compilou e rodou tudo sem reclamar");
        System.out.println("em rust o compilador recusaria o codigo com catch vazio");

    }
}
