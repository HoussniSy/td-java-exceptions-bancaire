# Réponses aux questions du TD

## Partie 3 – Pourquoi un `finally` ?
Le bloc `finally` s'exécute qu'il y ait succès, exception ou `return`. C'est l'endroit sûr pour libérer les ressources et signaler la fin du traitement. Dans une vraie application : connexions base de données, fichiers/flux, sockets réseau, verrous, transactions ouvertes, `Scanner` (ici). (`try-with-resources` fait la même chose automatiquement pour les `AutoCloseable`.)

## Partie 4 – Analyse (source = 2 000 €, montant = 500 €, panne après `debit()`)
1. **Soldes finaux** : source = 1 500 €, destination inchangée.
2. **Rollback automatique ?** Non. `try/catch` ne fait que gérer le flux de contrôle ; il n'annule aucune modification déjà faite en mémoire.
3. **Cohérent ?** Non : 500 € ont disparu (la somme des soldes a diminué).
4. **Redesign** : valider avant d'agir, entourer uniquement l'opération risquée, compenser (re-créditer la source) en cas d'échec, ou mieux utiliser une transaction atomique (tout ou rien).

## Partie 5 – Justification de la frontière d'exception
Le `try` entoure seulement `destination.credit(amount)`, car c'est la seule opération qui peut échouer *après* un effet de bord (le débit). Un `catch (Exception e)` global attraperait aussi les erreurs de validation (déjà gérées) et masquerait des bugs. On capte `RuntimeException` (pannes techniques), on compense, puis on relance une `BankingException` avec la cause d'origine ; si la compensation échoue, `addSuppressed` conserve les deux erreurs.

## Partie 6 – Option A ou B ?
- **Application utilisateur** : option B (catch spécifiques) → message précis et action adaptée (corriger le montant, changer de compte…), avec un `catch (BankingException)` final en filet de sécurité.
- **Couche de logs** : option A suffit (on enregistre tout ce qui est bancaire de la même façon).
- **Contrôleur d'API** : option A, en général via un gestionnaire global qui mappe la hiérarchie vers des codes HTTP (ex. 422 pour fonds insuffisants, 400 pour compte invalide), ou B si chaque cas a une réponse différente.

## Questions de discussion
1. **Exception métier vs `Exception`** : message et type explicites, `catch` ciblé, possibilité d'ajouter des champs (solde, limite).
2. **`extends RuntimeException`** : exception non vérifiée, plus de `throws` obligatoire ni de `catch` forcé. Code plus léger, mais le compilateur ne garantit plus que l'appelant gère l'erreur.
3. **Lancer / déclarer / gérer** : *lancer* = `throw` (créer et déclencher l'exception) ; *déclarer* = `throws` dans la signature ; *gérer* = `try/catch`.
4. **`catch (Exception e)` systématique** : masque les bugs (NullPointerException…), mélange erreurs métier et techniques, empêche des réactions adaptées, peut avaler des erreurs sans trace.
5. **Technique vs métier** : non, pas au même niveau. Les erreurs métier se traitent près de l'utilisateur (message clair) ; les erreurs techniques se journalisent et se traitent plus haut (page d'erreur générique, alerte).
6. **Capturer = cohérence ?** Non. Capturer évite le crash mais n'annule pas les effets déjà appliqués : il faut une compensation ou une transaction.
7. **En production** : les **transactions de base de données** (ACID, `commit`/`rollback`), par ex. `@Transactional` avec Spring/JPA, à la place de la compensation manuelle.
