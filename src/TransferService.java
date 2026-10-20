
public class TransferService {
    public static final double TRANSFER_LIMIT = 10000.0;

    public void transfer(BankAccount source, BankAccount destination, double amount) throws BankingException {

        // Validation : aucune modification des comptes avant la fin de cette partie
        if (amount <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif.");
        }
        if (source == null || destination == null) {
            throw new InvalidAccountException("Le compte source ou destination est absent.");
        }
        if (source == destination || source.getAccountNumber().equals(destination.getAccountNumber())) {
            throw new InvalidAccountException("Le compte source et le compte destination sont identiques.");
        }
        // Le plafond est testé avant le solde : 12 000 EUR doit donner "plafond", pas "solde"
        if (amount > TRANSFER_LIMIT) {
            throw new TransferLimitExceededException(amount, TRANSFER_LIMIT);
        }
        if (amount > source.getBalance()) {
            throw new InsufficientFundsException(source.getBalance(), amount);
        }

        // Exécution avec compensation
        source.debit(amount);
        try {
            // Le try entoure uniquement l'opération risquée
            destination.credit(amount);
        } catch (RuntimeException creditFailure) {
            try {
                source.credit(amount); // compensation : on rend l'argent à la source
            } catch (RuntimeException compensationFailure) {
                creditFailure.addSuppressed(compensationFailure);
                throw new BankingException(
                        "Échec du crédit ET de la compensation : intervention manuelle requise.", creditFailure);
            }
            throw new BankingException(
                    "Virement échoué : le débit a été annulé (source restaurée).", creditFailure);
        }
    }
}
