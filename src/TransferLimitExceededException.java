
public class TransferLimitExceededException extends BankingException {
    private double amount;
    private double limit;

    public TransferLimitExceededException(double amount, double limit) {
        super("Plafond dépassé : " + amount + " EUR demandés, limite = " + limit + " EUR");
        this.amount = amount;
        this.limit = limit;
    }

    // Getters
    public double getAmount() {
        return amount;
    }

    public double getLimit() {
        return limit;
    }
}
