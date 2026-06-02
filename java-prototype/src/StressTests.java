public class StressTests {

    public static void runAll() {
        System.out.println("=".repeat(70));
        System.out.println("  TESTES - JAVA");
        System.out.println("=".repeat(70));

        testeSaldoInsuficiente();
        testeContaInexistente();
        testeValorNegativo();
        testeRetryComSucesso();
        testeRetryEsgotado();
        testeTransferenciasSequenciais();
        testeBlastRadius();
    }

    // criar conta test
    private static Bank montarBanco() {
        Bank bank = new Bank();
        bank.addAccount(new Account("ACC-001", "Alice", 1000.0));
        bank.addAccount(new Account("ACC-002", "Bob", 500.0));
        bank.addAccount(new Account("ACC-003", "Carol", 200.0));
        return bank;
    }

    // Teste 1 -- Saldo Insuficiente
    private static void testeSaldoInsuficiente() {
        System.out.println("\n[TESTE 1] Saldo Insuficiente");
        Bank bank = montarBanco();

        try {
            bank.transfer("ACC-002", "ACC-001", 900.0);
        } catch (TransferError.InsufficientFunds e) {
            System.out.println("  OK - erro capturado");
            System.out.printf("  tinha: R$%.2f, tentou: R$%.2f%n", e.available, e.requested);
            System.out.printf("  saldo inalterado: R$%.2f%n", bank.balanceOf("ACC-002").orElse(0.0));
        } catch (TransferError e) {
            System.out.println("  FALHA: " + e.getMessage());
        }
    }

    // Teste 2 -- Conta Inexistente

    private static void testeContaInexistente() {
        System.out.println("\n[TESTE 2] Conta Inexistente");
        Bank bank = montarBanco();

        try {
            bank.transfer("ACC-999", "ACC-001", 100.0);
        } catch (TransferError.AccountNotFound e) {
            System.out.println("  OK - conta '" + e.accountId + "' nao encontrada");
        } catch (TransferError e) {
            System.out.println("  FALHA: " + e.getMessage());
        }
    }

    // Teste 3 -- Valor Negativo

    private static void testeValorNegativo() {
        System.out.println("\n[TESTE 3] Valor Negativo");
        Bank bank = montarBanco();

        try {
            bank.transfer("ACC-001", "ACC-002", -50.0);
        } catch (TransferError.NegativeAmount e) {
            System.out.printf("  OK - valor %.2f rejeitado%n", e.amount);
        } catch (TransferError e) {
            System.out.println("  FALHA: " + e.getMessage());
        }
    }

    // Teste 4 -- Queda de Rede

    private static void testeRetryComSucesso() {
        System.out.println("\n[TESTE 4] Retry de Rede - sucesso na 3a tentativa");
        Bank bank = montarBanco();

        try {
            double saldo = bank.transferWithRetry("ACC-001", "ACC-002", 300.0, 5, 2);
            System.out.printf("  OK - transferencia feita, saldo ACC-001: R$%.2f%n", saldo);
        } catch (TransferError e) {
            System.out.println("  FALHA: " + e.getMessage());
        }
    }
    // Teste 5 -- Tempo excedido - tentativas falha

    private static void testeRetryEsgotado() {
        System.out.println("\n[TESTE 5] Retry de Rede - todas as tentativas falham");
        Bank bank = montarBanco();

        try {
            bank.transferWithRetry("ACC-001", "ACC-002", 100.0, 3, 5);
        } catch (TransferError.NetworkTimeout e) {
            System.out.println("  OK - timeout apos " + e.retries + " tentativas");
            System.out.printf("  saldo inalterado: R$%.2f%n", bank.balanceOf("ACC-001").orElse(0.0));
        } catch (TransferError e) {
            System.out.println("  FALHA: " + e.getMessage());
        }
    }

    // Teste 6 -- tentativas em sequencias

    private static void testeTransferenciasSequenciais() {
        System.out.println("\n[TESTE 6] Transferencias Sequenciais - consistencia do saldo");
        Bank bank = montarBanco();

        double totalInicial = 1000.0 + 500.0 + 200.0;
        int ok = 0, falhou = 0;

        String[][] ops = {
                {"ACC-001", "ACC-002", "200.0"},
                {"ACC-001", "ACC-003", "300.0"},
                {"ACC-002", "ACC-003", "100.0"},
                {"ACC-001", "ACC-002", "600.0"},
        };

        for (String[] op : ops) {
            try {
                bank.transfer(op[0], op[1], Double.parseDouble(op[2]));
                ok++;
            } catch (TransferError e) {
                falhou++;
                System.out.println("  erro esperado: " + e.getMessage());
            }
        }

        double totalFinal = bank.balanceOf("ACC-001").orElse(0.0)
                + bank.balanceOf("ACC-002").orElse(0.0)
                + bank.balanceOf("ACC-003").orElse(0.0);

        System.out.println("  " + ok + " ok, " + falhou + " falhou");
        System.out.printf("  total: R$%.2f -> R$%.2f%n", totalInicial, totalFinal);

        if (Math.abs(totalInicial - totalFinal) < 0.001) {
            System.out.println("  OK - dinheiro conservado");
        } else {
            System.out.println("  PROBLEMA - saldo inconsistente!");
        }
    }

    // Teste 7

    private static void testeBlastRadius() {
        System.out.println("\n[TESTE 7] Blast Radius - erros isolados");
        Bank bank = montarBanco();

        int ok = 0, falhou = 0;

        Object[][] ops = {
                {"ACC-001", "ACC-002", 100.0},
                {"ACC-999", "ACC-001",  50.0},
                {"ACC-001", "ACC-002",  50.0},
                {"ACC-002", "ACC-001", 9999.0},
                {"ACC-001", "ACC-003",  50.0},
        };

        for (Object[] op : ops) {
            try {
                bank.transfer((String) op[0], (String) op[1], (double) op[2]);
                ok++;
            } catch (TransferError e) {
                falhou++;
            }
        }

        System.out.println("  " + ok + " ok, " + falhou + " erros");
        System.out.println("  sistema continua funcionando depois dos erros");
        System.out.println("  log de transacoes: " + bank.transactionLog.size() + " entradas");
    }
}