# TD – Gestion des exceptions Java : virement bancaire sécurisé

**Stratégie d'exceptions.** `TransferService.transfer(...)` valide tout *avant* de modifier les comptes : montant ≤ 0 → `IllegalArgumentException` ; compte null ou identique → `InvalidAccountException` ; > 10 000 € → `TransferLimitExceededException` ; solde insuffisant → `InsufficientFundsException`. Le plafond est testé avant le solde.
Les erreurs métier héritent de `BankingException` (exception vérifiée, déclarée avec `throws`, gérée par l'appelant).
`Main` utilise plusieurs `catch` spécifiques (saisie, fonds, plafond, compte) et un `finally` qui ferme le `Scanner`.

**Cohérence.** `try/catch` n'annule rien : si `credit()` échoue après `debit()`, l'argent disparaît. J'applique donc une **compensation** : le `try` entoure uniquement `credit()` ; en cas d'échec, la source est recréditée puis une `BankingException` est relancée avec l'erreur d'origine en `cause`.
Si la compensation échoue aussi, son erreur est ajoutée via `addSuppressed` (intervention manuelle requise). En production, on utiliserait une transaction de base de données (commit/rollback).
