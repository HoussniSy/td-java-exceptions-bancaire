
public class TransferTests {

    interface Operation {
        void run() throws Exception;
    }

    // Compte dont credit() tombe en panne (simule une défaillance technique)
    static class FaultyAccount extends BankAccount {
        public FaultyAccount(String accountNumber, double balance) {
            super(accountNumber, balance);
        }

        @Override
        public void credit(double amount) {
            throw new IllegalStateException("Panne simulée dans credit()");
        }
    }

    static int failures = 0;

    static Exception thrown(Operation op) {
        try {
            op.run();
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[ÉCHEC] ") + name);
        if (!ok) failures++;
    }

    public static void main(String[] args) {
        TransferService s = new TransferService();

        final BankAccount a1 = new BankAccount("A", 2000);
        final BankAccount b1 = new BankAccount("B", 0);
        Exception e = thrown(() -> s.transfer(a1, b1, 500));
        check("1. 500 sur 2000 -> succès, soldes 1500/500",
                e == null && a1.getBalance() == 1500 && b1.getBalance() == 500);

        final BankAccount a2 = new BankAccount("A", 2000);
        final BankAccount b2 = new BankAccount("B", 0);
        e = thrown(() -> s.transfer(a2, b2, 3000));
        check("2. 3000 sur 2000 -> InsufficientFundsException, soldes inchangés",
                e instanceof InsufficientFundsException && a2.getBalance() == 2000 && b2.getBalance() == 0);

        e = thrown(() -> s.transfer(a2, b2, 12000));
        check("3. 12000 -> TransferLimitExceededException (plafond avant solde)",
                e instanceof TransferLimitExceededException);

        e = thrown(() -> s.transfer(a2, b2, -100));
        check("4. -100 -> IllegalArgumentException", e instanceof IllegalArgumentException);

        e = thrown(() -> s.transfer(a2, a2, 100));
        check("5. même compte -> InvalidAccountException", e instanceof InvalidAccountException);

        e = thrown(() -> s.transfer(null, b2, 100));
        check("5b. source null -> InvalidAccountException", e instanceof InvalidAccountException);

        // 7. panne après debit, avant credit
        BankAccount src = new BankAccount("SRC", 2000);
        BankAccount dst = new FaultyAccount("DST", 0);
        e = thrown(() -> s.transfer(src, dst, 500));
        check("7. panne sur credit -> source restaurée à 2000, cause conservée",
                e instanceof BankingException && src.getBalance() == 2000
                        && e.getCause() instanceof IllegalStateException);

        // Bonus : la compensation échoue aussi
        BankAccount src2 = new FaultyAccount("SRC2", 2000);
        BankAccount dst2 = new FaultyAccount("DST2", 0);
        e = thrown(() -> s.transfer(src2, dst2, 500));
        check("8. compensation en échec -> cause + suppressed conservés",
                e instanceof BankingException && e.getCause() != null
                        && e.getCause().getSuppressed().length == 1);

        System.out.println(failures == 0 ? "Tous les tests passent." : "Échecs : " + failures);
    }
}
