package banking;

public class Main {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  PROTÓTIPO FINAL — Gestão de Erros como Design           ║");
        System.out.println("║  Linguagem: Java  |  Modelo: Checked Exceptions          ║");
        System.out.println("║  INF0288 LPP — UFG / INF — Junho de 2026                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        demonstracaoBasica();
        demonstracaoCatchVazio();
        StressTests.runAll();
        resumo();
    }

    // ─── 1. Transferência correta com tratamento explícito ────────────
    private static void demonstracaoBasica() {
        System.out.println("\n" + "─".repeat(60));
        System.out.println("  SEÇÃO 1: Fluxo normal com tratamento explícito");
        System.out.println("─".repeat(60));

        Bank bank = new Bank();
        bank.addAccount(new Account("ACC-001", "Alice", 1000.0));
        bank.addAccount(new Account("ACC-002", "Bob",   500.0));
        System.out.println("Contas: Alice R$1000,00 | Bob R$500,00\n");

        // O compilador EXIGE try-catch ou declaração throws — não há como ignorar
        try {
            double saldo = bank.transfer("ACC-001", "ACC-002", 300.0);
            System.out.printf("  [OK] Alice → Bob R$300,00. Saldo Alice: R$%.2f%n", saldo);
        } catch (TransferException e) {
            System.out.println("  [ERRO] " + e.getMessage());
        }

        try {
            bank.transfer("ACC-002", "ACC-001", 9000.0);
        } catch (TransferException.InsufficientFunds e) {
            System.out.printf("  [ERRO TRATADO] %s%n", e.getMessage());
            System.out.printf("  Saldo de Bob inalterado: R$%.2f%n",
                    bank.getAccount("ACC-002").getBalance());
        } catch (TransferException e) {
            System.out.println("  [ERRO] " + e.getMessage());
        }
    }

    // ─── 2. O catch vazio — argumento central do seminário ───────────
    private static void demonstracaoCatchVazio() {
        System.out.println("\n" + "─".repeat(60));
        System.out.println("  SEÇÃO 2: CATCH VAZIO — o argumento central");
        System.out.println("─".repeat(60));
        System.out.println("  Em Java, o compilador ACEITA engolir um erro silenciosamente.");
        System.out.println("  Em Rust, isso é IMPOSSÍVEL: Result ignorado = erro de compilação.\n");

        Bank bank = new Bank();
        bank.addAccount(new Account("ACC-001", "Alice", 500.0));
        bank.addAccount(new Account("ACC-002", "Bob",   500.0));

        double saldoAntes = bank.getAccount("ACC-001").getBalance();

        // Esta chamada vai falhar (saldo insuficiente), mas o erro
        // é engolido silenciosamente — o compilador não emite nenhum aviso.
        bank.transferSemTratar("ACC-001", "ACC-002", 9999.0);

        double saldoDepois = bank.getAccount("ACC-001").getBalance();

        System.out.printf("  Tentativa de transferir R$9999,00 com saldo de R$%.2f%n", saldoAntes);
        System.out.printf("  Saldo após a chamada: R$%.2f%n", saldoDepois);
        System.out.println("  A transferência FALHOU — mas nenhum sinal foi emitido.");
        System.out.println("  O sistema continua sem saber que a operação não ocorreu.\n");

        System.out.println("  Equivalente Rust (NÃO COMPILA — erro de compilação):");
        System.out.println("  ┌─────────────────────────────────────────────────────┐");
        System.out.println("  │  bank.transfer(\"ACC-001\", \"ACC-002\", 9999.0);        │");
        System.out.println("  │  // error[E0282]: unused `Result` that must be used  │");
        System.out.println("  │  // #[must_use] obriga o tratamento em compilação    │");
        System.out.println("  └─────────────────────────────────────────────────────┘");
    }

    // ─── 3. Resumo final ─────────────────────────────────────────────
    private static void resumo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  CONCLUSÃO");
        System.out.println("=".repeat(60));
        System.out.println("  Java:  confiabilidade depende da DISCIPLINA do programador.");
        System.out.println("         Um catch vazio compila e executa sem aviso algum.");
        System.out.println();
        System.out.println("  Rust:  confiabilidade é garantida pelo DESIGN da linguagem.");
        System.out.println("         Result<T,E> com #[must_use] torna erros silenciosos");
        System.out.println("         impossíveis por construção — não por convenção.");
        System.out.println("=".repeat(60));
    }
}
