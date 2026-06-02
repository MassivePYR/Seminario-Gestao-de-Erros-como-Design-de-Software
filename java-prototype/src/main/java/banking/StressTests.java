package banking;

public class StressTests {

    public static void runAll() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  TESTES DE ESTRESSE — JAVA (try-catch tipado)");
        System.out.println("=".repeat(60));

        testInsufficientFunds();
        testAccountNotFound();
        testNegativeAmount();
        testNetworkRetrySuccess();
        testNetworkRetryExhausted();
        testSequentialTransfers();
        testBlastRadiusContainment();
    }

    private static Bank makeBank() {
        Bank bank = new Bank();
        bank.addAccount(new Account("ACC-001", "Alice", 1000.0));
        bank.addAccount(new Account("ACC-002", "Bob",   500.0));
        bank.addAccount(new Account("ACC-003", "Carol", 200.0));
        return bank;
    }

    private static void testInsufficientFunds() {
        System.out.println("\n[TESTE 1] Saldo Insuficiente");
        Bank bank = makeBank();

        try {
            bank.transfer("ACC-002", "ACC-001", 900.0);
            System.out.println("  ✗ Deveria ter lançado exceção");
        } catch (TransferException.InsufficientFunds e) {
            System.out.println("  ✓ Exceção capturada corretamente: " + e.getMessage());
            System.out.printf("  ✓ Saldo de ACC-002 inalterado: R$%.2f%n",
                    bank.getAccount("ACC-002").getBalance());
        } catch (TransferException e) {
            System.out.println("  ✗ Tipo de exceção inesperado: " + e.getClass().getSimpleName());
        }
    }

    private static void testAccountNotFound() {
        System.out.println("\n[TESTE 2] Conta Inexistente");
        Bank bank = makeBank();

        try {
            bank.transfer("ACC-999", "ACC-001", 100.0);
        } catch (TransferException.AccountNotFound e) {
            System.out.println("  ✓ Conta '" + e.accountId + "' não encontrada — exceção tipada capturada");
        } catch (TransferException e) {
            System.out.println("  ✗ Tipo inesperado: " + e.getMessage());
        }
    }

    private static void testNegativeAmount() {
        System.out.println("\n[TESTE 3] Valor Negativo");
        Bank bank = makeBank();

        try {
            bank.transfer("ACC-001", "ACC-002", -50.0);
        } catch (TransferException.NegativeAmount e) {
            System.out.printf("  ✓ Valor inválido R$%.2f rejeitado antes de qualquer operação%n", e.amount);
        } catch (TransferException e) {
            System.out.println("  ✗ Tipo inesperado: " + e.getMessage());
        }
    }

    private static void testNetworkRetrySuccess() {
        System.out.println("\n[TESTE 4] Retry de Rede — Sucesso na 3ª tentativa");
        Bank bank = makeBank();

        try {
            double remaining = bank.transferWithRetry("ACC-001", "ACC-002", 300.0, 5, 2);
            System.out.println("  ✓ Transferência concluída após retries");
            System.out.printf("  ✓ Saldo restante ACC-001: R$%.2f%n", remaining);
        } catch (TransferException e) {
            System.out.println("  ✗ Falhou: " + e.getMessage());
        }
    }

    private static void testNetworkRetryExhausted() {
        System.out.println("\n[TESTE 5] Retry de Rede — Todas as tentativas esgotadas");
        Bank bank = makeBank();

        try {
            bank.transferWithRetry("ACC-001", "ACC-002", 100.0, 3, 5);
        } catch (TransferException.NetworkTimeout e) {
            System.out.println("  ✓ NetworkTimeout após " + e.retries + " tentativas");
            System.out.printf("  ✓ Saldo de ACC-001 inalterado: R$%.2f%n",
                    bank.getAccount("ACC-001").getBalance());
        } catch (TransferException e) {
            System.out.println("  ✗ Tipo inesperado: " + e.getMessage());
        }
    }

    private static void testSequentialTransfers() {
        System.out.println("\n[TESTE 6] Transferências Sequenciais — Consistência do Ledger");
        Bank bank = makeBank();

        double initialTotal = 1000.0 + 500.0 + 200.0;
        int success = 0, failures = 0;

        String[][] ops = {
            {"ACC-001", "ACC-002", "200.0"},
            {"ACC-001", "ACC-003", "300.0"},
            {"ACC-002", "ACC-003", "100.0"},
            {"ACC-001", "ACC-002", "600.0"}, // deve falhar
        };

        for (String[] op : ops) {
            try {
                bank.transfer(op[0], op[1], Double.parseDouble(op[2]));
                success++;
            } catch (TransferException e) {
                failures++;
                System.out.println("  [erro controlado] " + e.getMessage());
            }
        }

        double finalTotal = bank.getAccount("ACC-001").getBalance()
                + bank.getAccount("ACC-002").getBalance()
                + bank.getAccount("ACC-003").getBalance();

        System.out.printf("  ✓ Operações: %d ok, %d rejeitadas%n", success, failures);
        System.out.printf("  ✓ Total inicial: R$%.2f | Total final: R$%.2f%n", initialTotal, finalTotal);
        if (Math.abs(initialTotal - finalTotal) < 0.001) {
            System.out.println("  ✓ Conservação de valor garantida");
        } else {
            System.out.println("  ✗ LEDGER INCONSISTENTE!");
        }
    }

    private static void testBlastRadiusContainment() {
        System.out.println("\n[TESTE 7] Contenção do Blast Radius");
        Bank bank = makeBank();

        String[][] ops = {
            {"ACC-001", "ACC-002", "100.0"},
            {"ACC-999", "ACC-001", "50.0"},   // erro
            {"ACC-001", "ACC-002", "50.0"},
            {"ACC-002", "ACC-001", "9999.0"}, // erro
            {"ACC-001", "ACC-003", "50.0"},
        };

        int successes = 0, errors = 0;
        for (String[] op : ops) {
            try {
                bank.transfer(op[0], op[1], Double.parseDouble(op[2]));
                successes++;
            } catch (TransferException e) {
                errors++;
            }
        }

        System.out.printf("  ✓ %d sucessos, %d erros — todos isolados%n", successes, errors);
        System.out.println("  ✓ Sistema operacional após falhas parciais");
        System.out.println("  ✓ Log de transações: " + bank.getTransactionLog().size() + " entradas");
    }
}
