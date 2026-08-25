# Stage — Mohamed Radhi (juin – août 2026)

Ce document décrit le travail réalisé sur YAOI pendant le stage, sur la branche
`Stage_M_Radhi`. Il complète le `ReadMe.md` du projet et le `README-TEST.md`
(lancement et écriture des tests), auxquels il ne se substitue pas.

## Objectif

Rendre le greffon Eclipse utilisable : au début du stage, la validation
fonctionnait via le lanceur de tests (`LauncherUtils`) mais **pas** via le menu
`MOCL → Load → Validate` de l'éditeur. Le travail a porté sur trois axes :
réparer le greffon, compléter les primitives du langage, et rendre les messages
de violation exploitables.

## 1. Réparation du greffon

Le greffon s'exécutait sans détecter aucune violation. Le diagnostic a montré que
les contraintes du `.mocl` n'étaient jamais reliées au métamodèle : les contextes
(`context ...!....Process`) restaient non résolus, donc aucun invariant n'était
évalué.

Deux causes, corrigées dans `fr.enseeiht.yaoi.ui/.../handlers/Load.java` :

- **Le `.mocl` était chargé sans le moteur Xtext.** Il l'était dans le
  `ResourceSet` de l'éditeur réflexif, qui ne résout pas les références d'un
  langage Xtext. Il est désormais chargé dans un `XtextResourceSet` obtenu via
  `OclStandaloneSetup`, comme le fait `LauncherUtils`.
- **L'import n'était pas relié au métamodèle.** Le code se contentait de comparer
  le `nsURI` sans jamais appeler `setPackage`. Le `.ecore` est maintenant chargé
  dans le même `ResourceSet` et attaché à l'import.

Le handler `Validate.java` a été adapté en conséquence, avec une récupération plus
robuste du modèle `.xmi` (la sélection courante n'est pas toujours une sélection
d'arbre) et un rechargement du modèle pour l'aligner sur le métamodèle enregistré.

### Utilisation

Dans le **Sample Reflective Ecore Model Editor**, le modèle `.xmi` étant ouvert :

1. clic droit sur la racine → `Load Resource…` → le **métamodèle `.ecore`** ;
2. clic droit sur la racine → `MOCL → Load` → le fichier de **contraintes `.mocl`** ;
3. clic droit sur la racine → `MOCL → Validate` → la fenêtre liste les violations.

> Le `.mocl` doit impérativement être chargé par `MOCL → Load` et **non** par
> `Load Resource…` : ce dernier utilise le chargeur EMF standard, qui ne résout
> pas les références Xtext du fichier de contraintes.

## 2. Primitives ajoutées

Chaque opération est une classe de
`fr.enseeiht.ocl.xtext/.../ocl/operation/impl/` implémentant `IOclOperation`,
déclarée ensuite dans le registre `OclOperationEnum` (la constante de l'énumération
doit porter le nom utilisé dans les `.mocl`, la recherche se faisant par `valueOf`).

| Opération | Source | Remarque |
|---|---|---|
| `max`, `min` | nombre | forme binaire : `(3).max(5)` |
| `max`, `min` | collection | collection vide → `Invalid` |
| `at` | String, Sequence | index à partir de 1 ; hors bornes → `Invalid` |
| `last`, `reverse` | Sequence | |
| `including`, `excluding` | collection | conservent le genre de la collection |
| `intersection` | collection | `Set` si l'un des deux opérandes est un `Set` |
| `flatten` | collection | aplatissement récursif |
| `equalsIsIgnoreCase` | String | nom repris du registre existant |

Itérateur ajouté : **`sortedBy`** (`fr.enseeiht.ocl.xtext/.../ocl/iterators/impl/`),
enregistré dans `OclIteratorEnum`.

Ces ajouts sont couverts par des tests unitaires dans
`TestsUnitaires/tests/`, suivant les conventions de `README-TEST.md`
(préfixes `ok-`, `v-`, `vu-`).

> **Point d'attention lexical.** Un littéral entier suivi d'un point est lu comme
> un réel (`3.`), donc `3.max(5)` échoue au parsing. Écrire `(3).max(5)`.

### Non traité

L'itérateur **`closure`** n'a pas été implémenté. L'architecture actuelle
pré-évalue le corps de l'itérateur une seule fois, sur les éléments de la
collection source ; une fermeture transitive impose de le ré-évaluer sur les
éléments découverts au fur et à mesure. L'ajouter suppose donc une évolution du
moteur d'évaluation, à discuter avant implémentation.

Restent également à faire : `characters`, `product`, `selectByKind`,
`selectByType`, `symmetricDifference`, `append`, `prepend`, `insertAt`,
`subOrderedSet`.

## 3. Messages de violation

Les violations affichaient l'objet Java brut :

```
nomNonVide failed for object org.eclipse.emf.ecore.impl.DynamicEObjectImpl@3dd88a
(eClass: org.eclipse.emf.ecore.impl.EClassImpl@7df231c7 (name: Composant) ...)
```

La description ne cherchait qu'un attribut nommé exactement `name` ; tout
métamodèle utilisant `nom`, `id` ou `titre` retombait sur le `toString()` par
défaut d'EMF. `ValidationFailed` et `ValidationUndefined` produisent désormais :

```
nomNonVide : viole par Composant (nom vide)
auMoinsUnPort : viole par Composant "C1"
nomsInstancesUniques : viole par Netlist "CircuitCasse"
```

La description retenue est le nom du type, suivi de l'identifiant de l'élément
lorsqu'il existe (`name`, `nom`, `id`, `titre`, `label`), ou de la mention
explicite que cet identifiant est vide — le cas le plus fréquent en pratique.
Les messages personnalisés (`inv nom('message'): …`) restent prioritaires et
inchangés.

## Validation du travail

- Suite de tests complète au vert, incluant les tests des primitives ajoutées.
- Greffon vérifié de bout en bout sur SimplePDL, puis sur un métamodèle de
  circuits électroniques (catalogue de composants et netlist) : un modèle fautif
  fait apparaître la liste des violations, un modèle correct affiche
  « Validation Success ».
