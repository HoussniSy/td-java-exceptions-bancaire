
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        scanner.useLocale(Locale.US); // séparateur décimal = point

        BankAccount source = new BankAccount("FR76-0001", 2000.0);
        BankAccount destination = new BankAccount("FR76-0002", 500.0);
        System.out.println("Compte source : " + source);
        System.out.println("Compte destination : " + destination);

        try {
            System.out.print("Montant du virement en EUR (point pour les décimales) : ");
            double amount = scanner.nextDouble();

            TransferService service = new TransferService();
            service.transfer(source, destination, amount);

            System.out.println("Virement réussi.");
            System.out.println("Compte source : " + source);
            System.out.println("Compte destination : " + destination);

        } catch (InputMismatchException e) {
            System.out.println("Erreur de saisie : veuillez entrer un nombre (ex. 500 ou 49.90).");
        } catch (IllegalArgumentException e) {
            System.out.println("Montant invalide : " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println(e.getMessage());
        } catch (TransferLimitExceededException e) {
            System.out.println(e.getMessage());
        } catch (InvalidAccountException e) {
            System.out.println("Compte invalide : " + e.getMessage());
        } catch (BankingException e) {
            // Autres erreurs bancaires (ex. échec du crédit)
            System.out.println("Erreur bancaire : " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Cause : " + e.getCause().getMessage());
            }
        } finally {
            System.out.println("Transaction processing completed.");
            scanner.close();
        }
    }
}
