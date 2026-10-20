
public class InsufficientFundsException extends BankingException {
    private double balance;
    private double amount;

    public InsufficientFundsException(double balance, double amount) {
        super("Solde insuffisant : solde = " + balance + " EUR, montant demandé = " + amount + " EUR");
        this.balance = balance;
        this.amount = amount;
    }

    // Getters
    public double getBalance() {
        return balance;
    }

    public double getAmount() {
        return amount;
    }
}
