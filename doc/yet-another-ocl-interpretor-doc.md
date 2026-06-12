Documentation de YAOI
========

## Sommaire

- [Fonctionnement du projet](#Fonctionnement-du-projet)
  - [Syntaxe Xtext](#Syntaxe-Xtext)
  - [Manipulation des fichiers OCL](#Manipulation-des-fichiers-OCL)
- [Typage](#Typage)
    - [Typage des invariants et des operations définis par l’utilisateur](#Typage-des-invariants-et-des-operations-définis-par-l’utilisateur)
    - [Gestion des erreurs de type](#Gestion-des-erreurs-de-type)
    - [Liste des classes utilisées dans le typage](#Liste-des-classes-utilisées-dans-le-typage)
    - [Modification à apporter au typage](#Modification-à-apporter-au-typage)
- [Interpreteur](#Interpreteur)
    - [Equivalence entre les types OCL et les classes JAVA](#Equivalence-entre-les-types-OCL-et-les-classes-JAVA)
    - [Erreurs à l’interpretation](#Erreurs-à-l’interpretation)
    - [Opérations](#Operations)
    - [Itération (IteratorExp, IterateExp)](#Itération-IteratorExp-IterateExp)
- [JET et JMerge](#JET-et-JMerge)
- [Editeur Xtext](#Editeur-Xtext)
    - [Rajout d’erreur dans l’éditeur](#Rajout-d’erreur-dans-l’éditeur)
    - [Scoping personnalisé](#Scoping-personnalisé)
    - [Auto-Complétion](#Auto-Complétion)
    - [Outline](#Outline)
- [Architecture de tests](#Architecture-de-tests)
    - [Structure](#Structure)
    - [Lancement des tests](#Lancement-des-tests)
    - [Tests Unitaires](#Tests-Unitaires)
- [Trucs et Astuces](#Trucs-et-Astuces)
    - [Ajouter un type](#Ajouter-un-type)
    - [Ajouter une opération de base](#Ajouter-une-opération-de-base)
    - [Ajouter un itérateur](#Ajouter-un-itérateur)
== Méta ==

règles de mise en forme:
noms de classe / éléments de syntaxe en *italique*
noms de fichier / de packages en ``code``

========

Ce projet se base sur [le standard OMG version 2.4](https://www.omg.org/spec/OCL/2.4/PDF).

## Fonctionnement du projet

### Syntaxe Xtext
cf `fr.enseeiht.ocl.xtext`
(expliquer que Xtext engendre Ecore)
Le projet se base sur une synatxe Xtext contenue dans le fichier ``Ocl.xtext``, celle ci engendre un métamodèle Ecore des fichers .mocl. La génration est lancée par le workflow mwe2 ``GenerateOcl.mwe2``.
### Manipulation des fichiers OCL
cf ``fr.enseeiht.ocl.xtext.ocl.adapter``

Pour pouvoir manipuler les éléments de syntaxe des fichier mocl, un *ValidationAdapter* a été associé à chaque élément.
Ces classes implémentent l'interface *OCLAdapter* qui défini principalement les méthodes *getType* et *getValue*, qui sont respectivement utilisées pour l'interprétation et le typage. 

La méthode *getValue* prends en argument l'objet concerné par l'invariant testé, soi la valeur de self dans le contexte.

Pour créer un adapter il faut appeller la ``OCLValidationAdapterFactory`` qui se charge d'associer le bon Adapter à l'élément de syntaxe voulu.

Par exemple imaginon que nous travaillons sur un invariant défini dans la syntaxe par :
```Xtext
OclInvariant:
	'inv' name=ID ('('errorMessage=OclExpression')')?':' body=OclExpression
;
```

Si on veut récupérer le type de son *body* il suffit de faire :
```Java
OCLAdapter bodyAdapter = OCLValidationAdapterFactory.INSTANCE.createAdapter(target.getBody());
OclType bodyType = bodyAdapter.getType();
```



Ces classes sont générées lors de l'execution du workflow mwe2 graçe aux templates JET (cf . [JET](#JET-et-JMerge)). 
Note : Il aurait été possible de rajouter directement ces méthodes dans les classes générées par le modèle Ecore mais le workflow MWE2 écrase toute les modifications faites à la syntaxe à chaque exécution, d'où l'utilisation du patron de conception Adapter. 

## Typage

Le typage se divise en deux parties. Chaque type d'OCL est représenté par une classe qui implément l'interface *OclType*. cette interface définit trois méthode : 
- *conformsTo(OclType)* qui indique si le type courant se conforme à un autre. Par exemple, puisque *Integer* se conforme à *Real*, on aura ``oclInteger.conformsTo(new OclReal) == true``. cette méthode est réflexive (``type.conformsTo(type) == true``) mais pas symétrique.
- *unifyWith(OclType)* qui retourne le super-type commun le "plus petit" (à reformuler). Le plus grand super-type étant *OclAny*, l'unification de deux types incompatibles résultera en *OclAny*. De plus, toute unification avec *OclInvalid* retournera *OclInvalid*. Cette méthode est symétrique (``type1.unifyWith(type2) == type2.unifyWith(type1)``), et réflexive (``type.unifyWith(type) == type``).
- *toString()* qui est utilisé pour les messages d'erreur principalement.

Ces types sont manipulés dans les *ValidationAdapters* dans leurs méthodes *getType()*. Cette méthode retourne le type de l'élement syntaxique. S'il y a une erreur de typage, la méthode retournera un *OclInvalid* contenant les erreurs potentiellement soulevées.

### Typage des invariants et des operations définis par l'utilisateur

Puisque les restrictions sur les opération du modèle (EOperation) n'ont pas été implémentées, l'utilisateur peut soit déclarer un invariant contextualisé, soit définir une opération ou un attribut, avec ou sans contexte. Voici un résumé de la méthode de typage : 
- ***Invariants:*** l'expression d'un invariant doit toujours être un Booleen. Il est absurde d'écrire `inv inv1 = "string"`, et donc le typage de l'invariant renvoie une erreur si l'expression à droite ne se conforme pas à un booleen. Puisque `null` se conforme à tous les autres types, il est possible d'écrire (au sens du typage) `inv inv1 = null`. L'erreur ne se produira qu'à l'exécution.
- ***Attributs:*** le typage des attributs se fait de la même manière que les invariants. `def: att : Type = exp` n'est valide qu'à la condition que le type de `exp` se conforme au type représenté par `Type`. Il est important de noter que le type de `Type` est un *OclClassifier* qui représente le type `Type` (voir *OclClassiier dans la liste ci-dessous).
- ***Opérations:*** pendant la déclaration, la vérification du type d'une opération est exactement identique à celle d'un attribut (le litéral `Invalid` n'existant pas dans la syntaxe). Pendant l'appel de l'opération cependant, il y a une vérification du type des paramètres qui s'opère. Par exemple, `operation(arg)` n'est valide que si `arg` se conforme au type du paramètre déclaré initialement. Dans ce cas, le type de retour sera le type déclaré (et non le type de son expression !).

Pendant l'évaluation du type des expressions contextualisées, il arrive à certains moments que `self` soit utilisé. Dans ce cas, notre méthode pour obtenir le type du contexte en arrive à typer la ligne de déclaration du contexte `context model!class`. Pour cette raison, le type de cette déclaration est la classe `model!class` du modèle.

### Gestion des erreurs de type
*cf `fr.enseeiht.ocl.xtext.types/OclInvalid, fr.enseeiht.ocl.xtext.validation`*

Toute la gestion des erreurs de type passe par *OclInvalid* et par les classes d'erreur qu'il contient. De manière très simple, si une opération invalide au sens du typage a lieu, le type de l'expression deviendra un *OclInvalid* qui contiendra une *TypeCheckingError* appropriée. Il y a plusieurs classes qui héritent de *TypeCheckingError*, et elles représentent chacune un type d'erreur particulier (par exemple *TypeMismatchError*). Même si le type se conforme à tout, il est obligatoire de le laisser "absorber" tout autre type pendant l'unification afin de faire remonter les erreurs (sans quoi elles disparaîteraient à la première opération).

Puisque toute opération sur un type Invalide est licite (Invalid se conformant à tout), il n'y aura jamais de *TypeMismatchError* avec un type invalide. A la place, l'opération aura le type *OclInvalid* avec comme erreur source l'erreur soulevée dans l'expression.

Ensuite, le *OclTypeChecker* va évaluer la validité de chacuns des invariants, attributs et opérations, puis enregistrer tous les types *OclInvalid* qu'il reçoit dans une liste. Cette liste sera aplatie avant d'être donnée au validateur global, qui communiquera toutes les expressions sources d'erreur.

### Liste des classes utilisées dans le typage
*cf ``fr.enseeiht.ocl.xtext.types``*

Cette liste compile les différents types, ce qu'ils représentent, ainsi que leur conformance et toute unification remarquable. 

#### OclAny
*OclAny* est la super-classe de toute les autres. Elle est comparable à *Object* en java, et représente les objets non-déterminés.
- ***Conformance:*** puisque *OclAny* et la plus grande super-classe, elle ne se conforme qu'avec *OclAny*
- ***Unification:*** pour les mêmes raisons, *OclAny* s'unifie en *OclAny* avec tous les types valides.

#### OclVoid
*OclVoid* représente le type vide, avec pour seule valeur *null*. Pour le typage, *OclVoid* n'est pas vraiment utile et doit donc disparaître quand il s'unifie avec qun type différent. *null* n'aura en réalité d'utilité que dans la valuation des expressions.
- ***Conformance:*** *OclVoid* se conforme à tous les autres types.
- ***Unification:*** pour tout type *OclType* donné, *OclVoid* s'unifie avec lui et donne le même *OclType* : ``oclVoid.unifyWith(oclType) == oclType``.

#### OclInvalid
*OclInvalid* est le type d'erreur utilisé par *yaoi*. C'est alors le seul type portant des données hors de son typage. Il est d'évidence renvoyé aux échecs de conformance de type.
- ***Conformance:*** *OclInvalid* se conforme à tous les types.
- ***Unification:*** afin d'empoisonner tout type correct (permettant la remontée), toute unification par ou avec *OclInvalid* renvoie ce dernier.

#### OclClassifier
*OclClassifier* est un type utilisé dans la déclaration de type d'autre éléments, comme le type de retour d'une opération, ou le type d'un paramètre. ce type représente la *déclaration du type*, mais pas le type lui-même. Ansi, quand on écrit ``def: attribut : Integer = 1``, le type de ``Integer`` sera un *OclClassifier* qui *représente* le type *Integer*, et non le type *Integer* lui-même. Pour obtenir le type représenté, utiliser ``getRepresentedType()``.
- ***Conformance:*** *OclClassifier* ne se conforme qu'aux autre *OclClassifier* si le type représenter par le premier se conforme au second.
- ***Unification:*** comme avec la conformance, *OclClassifier* s'unifie avec un autre *OclClassifier* et donne un *OclClassifier* qui représente l'unification des types représentés.

#### OclReal
*OclReal* représente les réels, appelés *Real* dans la syntaxe OCL. Les réels sont de la forme 
```(INT '.' INT| '.' INT | INT '.') ('e' ('-'|'+') INT)?``` dans la syntaxe, et *OclReal* possède comme sous-type *OclInteger*.
- ***Conformance:*** *OclReal* ne se conforme qu'avec les autres *OclReal* (et *OclAny*).
- ***Unification:*** puisque *OclInteger* est un sous-type de *OclReal*, l'unification des deux types donne *OclReal*. Avec tous les autres types non réels, elle résulte en *OclAny*.

#### OclInteger
*OclInteger* représente les entiers, appelés *Integers* dans la syntaxe OCL. Il est le sous-type de OclReal, et toutes les opérations définies sur les réels qui n'ont pas été redéfinies le sont également sur les entiers. (à confirmer)
- ***Conformance:*** *OclInteger* se conforme avec *OclReal* à cause de son héritage. Autrement, il ne se conforme qu'avec lui-même et *OclAny*.
- ***Unification:*** symétriquement, *OclInteger* s'unifie en *OclReal* avec *Oclreal*. Avce tout autre type non-réel, l'unification donne *OclAny*.

#### OclString
*OclString* représente les chaînes de caractères, appelé "String" dans la syntaxe OCL. 
- ***Conformance:*** *OclString* ne se conforme qu'avec lui-même et *oclAny*.
- ***Unification:*** *OclString* s'unifie en *OclAny* avec tous les autres types.

#### OclBoolean
*OclBoolean* représente les types booléens avec comme valeur ``true`` ou ``false``. 
- ***Conformance:*** *OclBoolean* ne se conforme qu'avec lui-même et *oclAny*.
- ***Unification:*** *OclBoolean* s'unifie en *OclAny* avec tous les autres types.

#### OclCollection
Cette classe est une classe abstraite dont les types concrets *OclBag*, *OclOrderedSet*, *OclSequence* et *OclSet* héritent. Il n'est donc pas possible de créer une *Collection* OCL comme on pourrait créér un *Set*. En revanche, ce type est utilisable dans les types de retour des *Feature Definitions*, des opérations ou dans le type de leurs paramètres, en tant que *OclClassifier*. Pour obtenir le type des éléments d'une collection il suffit d'appeler la méthode ``getSubtype()``, et pour le modifier il faut appeler ``setSubtype()``.
- ***Conformance:*** la conformance entre deux *OclCollection* est conditionnelle : il n'y a conformance que si le type des éléments de la première *OclCollection* se conforme à celui des éléments de la deuxième.
- ***Unification:*** puisque *OclBag*, *OclOrderedSet*, *OclSequence* et *OclSet* sont des sous-types de *OclCollection*, l'unification d'une *OclCollection* avec un de ses sous-types résulte en une *OclCollection*. Ensuite, le type des éléments du résultat deviendra l'unification des types des éléments des deux collections. 

Aucun de ses sous-types ne se conforment entre eux, et s'unifient en une *OclCollection*. Par exemple, *OclBag(...)* et *OclSet(...)* ne se conforment pas entre eux (quel que soit le type de leurs éléments) et s'unifient en une *OclCollection(...)* dont le type de ses éléments sera l'unification de ceux des deux autres types.
Puisque les quatres sous-types se comportent de la même manière, la conformance et l'unification seront omises.

#### OclBag
*OclBag* est une des quatres sous-classe de OclCollection, et représente un ensemble d'éléments avec possibilité d'avoir plusieurs fois le même élément. Les *Bag*, tels que décrits dans la syntaxe d'OCL, ne présentent pas d'ordre dans leurs éléments.
- ***Conformance:*** voir *OclCollection*.
- ***Unification:*** voir *OclCollection*.

#### OclOrderedSet
*OclOrderedSet* est une des quatres sous-classe de OclCollection, et représente un ensemble d'éléments au niveau mathématique, sans ordre ni doublons.
- ***Conformance:*** voir *OclCollection*.
- ***Unification:*** voir *OclCollection*.

#### OclSequence
*OclSequence* est une des quatres sous-classe de OclCollection, et représente un ensemble ordonné d'éléments avec possibilité d'avoir plusieurs fois le même élément.
- ***Conformance:*** voir *OclCollection*.
- ***Unification:*** voir *OclCollection*.

#### OclSet
*OclSet* est une des quatres sous-classe de OclCollection, et représente un ensemble d'éléments au niveau mathématique, sans ordre ni doublons.
- ***Conformance:*** voir *OclCollection*.
- ***Unification:*** voir *OclCollection*.

#### OclEClass
*OclEclass* représente toutes les classes EMF. Par exemple, dans `self.name`, `self` deviendra une *OclEClass* qui aura un attribut `classType` initialisé avec le type de self dans le modèle (dans le cas où `self` a une multiplicité supérieure à 1, on aura une *OclCollection* de *OclEClass*, ce qui n'est pas encore géré par ce projet). Il y a également une méthode ``findLowestSupertype(OclEClass)`` qui permet de trouver la superclasse au sens du modèle la plus petite entre deux *EClasses* et l'ajouter dans une *OclEClass* (une sorte d'unification).
- ***Conformance:*** ne se conforme avec une autre *OclEClass* quand son `classType` est une sous *EClass* de l'autre.
- ***Unification:*** unifiée à une autre *OclEClass*, utilise ``findLowestSupertype(OclEClass)`` pour trouver la plus grande super-classe qui englobe les deux `classType`. 

Contrairement aux types précédent, ce type n'a pas de litéral syntaxique (on ne peut pas écrire `def: att : EClass(...) = ...`). Pour l'utiliser dans la signature d'opération, il faudra utiliser la navigation (par exemple `def: att : self.eClasse = ...`).

#### OclEnum
Cette classe sert à représenter les *EEnums*, et possède un attibut `enumLit` qui correspond à l'*EEnum* d'ecore. Ce type, à l'instar de *OclEClasse*, n'a pas de litéral pour le typage et devra donc être manipulé de la même manière. Pour obtenir `enumLit`, utiliser `getenumLit()`.
- ***Conformance:*** la conformance est particulièrement restrictive dans cette classe. Une *OclEnum* ne se conforme à une autre que si elles sont égales.
- ***Unification:***  en suivant le même principe, une *OclEnum* s'unifie en *OclAny* avec toutes les *OclEnums* dont l'`enumLit` n'est pas égale à la sienne.

#### OclTuple
Ce type représente les tuples au sens d'OCL (de la forme `Tuple{x:Integer, y:Integer}` par exemple). Pour le moment, les seules fonctionnalités du type sont la déclaration du *OclClassifier*, la déclaration du litéral, et l'accès aux valeurs du tuple (ex :`tuple.x`). Aucune autre opération n'a été faite. Il n'y a pas de méthode pour accéder aux clés et valeurs du type.
- ***Conformance:*** Le conformance entre deux *OclTuple* n'est vraie que si les deux tuples ont exactement les mêmes clés, et si le type associé à chaque clé se conforme au type de l'autre tuple. Ainsi, `Tuple{x:Integer}` ne se conforme pas avec `Tuple{x:Real, y:Real}`. La conformance n'était possible que si le deuxième tuple n'avait pas `y:Real`.
- ***Unification:*** en suivant un principe similaire à la conformance, deux *OclTuple* qui n'ont pas exactement les mêmes clés s'unifient en *OclAny*. S'il y a égalité, ils s'unifient en un *OclTuple* dont les valeurs sont le résultat de l'unification des valeurs des deux tuples.

#### OclMap
*OclMap* est un type qui associe des clés aui partagent toutes le même type, à des valeurs qui partagent également un même type. De ce fait, `Map{1="x", true="y"}` aura pour type `Map(OclAny, String)`. Il n'y a pour le moment pas moyen d'accéder aux types des clés et valeurs, et aucune opération n'a été réalisée pour ce type de basse priorité.
- ***Conformance:*** comme pour les collections, uen *OclMap* se conforme à une autre que dans le cas ou le type des clés se conforme à celui des clés de l'autre map, idem pour le type des valuers.
- ***Unification:*** deux *OclMap* s'unifient en une *OclMap* dont le type des clés et des valeurs sont unifiés.

#### OclTypePair
Il s'agit de la seule classe qui ne représente pas de type dans OCL. Cette classe est nécessaire pour obtenir le type d'une *MapElement* telle que décrite dans la syntaxe, avec par exemple`Map{0:Integer="text"}`. Dans ce cas, lee *MapElement* contient `0:Integer="text"`, et pour décider du type de cet élément, il est pratique d'utiiser une classe qui représente une paire de types (qui contiendra alors `Integer` et `String`). Elle a deux attributs `left` et `right` accessible à travers `getLeft()` et `getRight()`.
- ***Conformance:*** Cette classe se conforme à une autre *OclTuplePair* que si les deux types se conforment aux deux autres. Cette conformance n'a pas de but concret car elle n'est jamais sensée être conformée.
- ***Unification:*** Comme pour les *OclMap*, l'unification de deux *OclTypePair* résulte en une paire dont les deux types sont unifiés.

### Modification à apporter au typage

#### Conversion entre Type Ecore et Type Ocl

Lors du parcours d'un model un Type primitif peut etre retrouné (Int, String, etc...). Par exemple : 

```=
context library!library.Book
inv test:
  self.pages > 0
```

Dans ce cas-là, l'attribut `pages` de *Book* est un **EInt** (Ecore Int). Il faut donc convertir ce type en **OclInt**. Pour le moment cette conversion est faites avec un simple switch qui peut facilement casser (fichier : `fr.enseeiht.ocl.xtext/jet-gen/fr/enseeiht/ocl/xtext/ocl/adapter/impl/NavigationOrAttributeCallValidationAdapter.java`; ligne : `148`) : 

```java=
switch (eDataType.getClassifierID()) {
case EcorePackage.EBOOLEAN:
    type = new OclBoolean();
    break;
case EcorePackage.EINT:
    type = new OclInteger();
    break;
case EcorePackage.ESTRING:
    type = new OclString();
    break;
default:
    throw new IllegalArgumentException("Unimplemented type: " + eDataType.getInstanceClassName());
}
```

Il est nécessaire de le modifier pour prendre en compte tout les types. 

## Interpreteur

Pour valider un modele à l'aide d'un fichier mocl on parcours chaque invariant et récupère sa valeur, valeur qui peut soit être un booléen soit une erreur (cf. [Erreurs à l’interpretation](#Erreurs-à-l’interpretation)). La récupération de cette valeur se fait à partir des méthodes *getValue* des adapter. En appellant celui de *OclInvariantValidationAdapter* on parcours entièrement l'expression qui le compose.

### Equivalence entre les types OCL et les classes JAVA
|Type OCL|Type JAVA|
|-|-|
|OclReal|java.lang.Double|
|OclInteger|java.lang.Integer|
|OclString|java.lang.String|
|OclBoolean|java.lang.Boolean|
|OclBag|org.apache.commons.collections.bag.HashBag|
|OclOrderedSet|fr.enseeiht.ocl.xtext.utils.SetUniqueArrayList|
|OclSequence|java.util.List|
|OclSet|java.util.Set|
|OclEnum|org.eclipse.emf.ecore.impl.EEnumLiteralImpl|
|OclTuple|None*|
|OclMap|None*|
|OclTypePair|None*|

*Ces types n'ont pas été implémenté dans cette version du projet
### Erreurs à l'interpretation
*cf``fr.enseeiht.ocl.xtext.ocl.adapter``* 
Dans certains cas de figure l'interpretation des invariants déclenche des erreurs qui n'aurait pas put être évitée par le typage, comme par exemple dans le cas d'une division par zero, ou l'accès à un attribut dont la valeur n'es pas renseignée dans le modèle testé.
Pour représenter cela les adapter renvoie un objet de type *Invalid*.

*Invalid* est une classe abstraite dont chaque sous-classe représente une erreur précise.

Les erreurs doivent être remontées dès qu'elle sont rencontrée pour s'assurer qu'elle soit récupérée par l'interpréteur. 

Aussi des erreurs qui doivent être évités par le typage sont implémentées à des fin de débug.
### Operations
cf. ``fr.enseeiht.ocl.xtext.ocl.operation``

Les opérations dites basiques (celles qui ne sont pas définies par l'utilisateur) sont décrite par l'interface *IOclOperation*.

La résolution des opérations est effectuée par la classe *OpertationResolutionUtils*.

Plusieurs opérations basiques sont implémentées dans le package ``fr.enseeiht.ocl.xtext.ocl.operation.impl``, il est possible de rechercher des operations grâce à l'enum *OclOperationEnum*.

L'interface *IOclOperation* définit la méthode *getReturnValue* pour l'interpretation et cinq autres methodes pour le typage et la résolution des appels.

#### Liste des operations implémentées 


|Type source| Operations Implémentées | Operations Non Implémentées |
| -------- | -------- | -------- |
| EClass     |allInsctances()     | - |
| OclVoid,OclAny,Invalid   |oclAsSet(), oclIsUndefined(), oclAsType(), oclIsTypeOf(), oclIsKindOf()    |oclType()  |
| Number     |toString()     | max(), min() |
| Boolean     |toString()     | - |
|String|concat(),substring(),toInteger(),toReal(),matches()|toBoolean(), toUpperCase(), toLowerCase(), indexOf(), at(), equalsIsIgnoreCase(), characters()|
| Collection     | size(), includes() | min(), max(), exludes(), includesAll(), exludesAll(), count(), isEmpty(), notEmpty(), sum(), product(), selectByKind(), selectByType(), asSet(), asOrderedSet(), asBag(), flatten()|
|Set & Bag|union()|intersection(), including(), excluding(), symmetricDifference()|
|OrderedSet|-|indexOf(), at(), subOrederedSet()|
|OrderedSet & Sequence|first()|append(), prepend(), insertAt(), last(), reverse()|


[Comment ajouter une opération](#Ajouter-une-opération-de-base) 

### Itération (IteratorExp, IterateExp)
*cf ``fr.enseeiht.ocl.xtext.ocl.iterators``*

Le comportement de *IterateExp* est défini dans *OclIterate*.

Le comportement des différents *IteratorExp* est défini sous la forme d'implémentations de l'interface *OclIterator*. L'implémentation suit la spécification OCL en faisant appel au comportement de *OclIterate* et des autres *OclIterator*: *isUnique* fera appel à *collect* et à *forAll*.

*OclIteratorEnum* permet d'instancier des *OclIterator* à partir de leur nom.

[Comment ajouter un itérateur](#Ajouter-un-itérateur) 

## JET et JMerge
*cf ``fr.enseeiht.ocl.xtext.templates``, ``fr.enseeiht.ocl.xtext.ocl.adapter.templates``*

JET (Java Emitter Templates) est un outil de génération de code Java. Il se base sur un fichier JET (.javajet) qui engendre un fichier Java "template". La génération dans Eclipse se fait automatiquement à la modification du JET. L'exécution de la classe "template" engendre une ou plusieurs fichiers Java "output". Voir [ce tutoriel](https://www.eclipse.org/articles/Article-JET/jet_tutorial1.html).

Nous utilisons JET pour générer les *ValidationAdapters* et la *AdapterFactory*.

Lors de l'exécution du workflow MWE2, les templates sont executés pour générer les outputs.

Puisque les *ValidationAdapters* sont nécessairement édités au cours du développement du projet, nous utilisons JMerge pour combiner les modifications JET et les nôtres.

Pour cela, les méthodes sont générées avec l'annotation ``@generated``. Lorsqu'une méthode est modifiée, l'annotation doit être modifiée en ``@generated NOT`` pour signaler à JMerge qu'il ne faut pas l'écraser.


<img src="https://hedgedoc.inpt.fr/uploads/6a13e504-069a-4e68-a2f6-d3cce1491c5a.png" alt="drawing" width="500"/>

## Editeur Xtext

Xtext propose des outils d'aide à la personnalisation du langage et de son éditeur. En premier, XText propose 2 personnalisations majeures du langage : 
- La remontée d'erreurs personnalisées. [Rajout d'erreur dans l'éditeur](#Rajout-d%E2%80%99erreur-dans-l%E2%80%99%C3%A9diteur).
- La personnalisation du scope des références définies dans la syntaxe. [Scoping personnalisé](#Scoping-personnalis%C3%A9).

Cependant, il existe la possibilité de personnaliser énormément de composantes utiles à traduction des langages, pour cela il faut `Override` la méthode `bind{ComposantName}()` et renvoyer votre classe.

<u>NDLR</u> : je n'ai jamais touché à ça, et je n'ai pas trouvé de docs sur ca, donc bonne chance si vous deviez y toucher .

On trouve, aussi, de nombreuses personnalisations au niveau de l'éditeur comme décrites dans [cette documentation](https://eclipse.dev/Xtext/documentation/310_eclipse_support.html) (pas totalement à jour). Seule une partie est aujourd'hui implémenté : 
- [Auto-Complétion](#Auto-Complétion).
- [Outline](#Outline).



### Rajout d'erreur dans l'éditeur
*cf `fr.enseeiht.ocl.xtext.validation.OclValidator` in `fr.enseeiht.ocl.xtext` project*

Xtext laisse la possiblité de rajouter des erreurs personnalisées qui seront rajoutée à la ressource (le mocl) et remontée au niveau de l'éditeur. Pour cela, il faut rajouter une méthode dans le fichier *OclValidator* correspondant à ce qu'on veut tester.

<u>Passage de la doc XText</u> : 

Si vous voulez faire une règle comme quoi les noms doivent commencer par une capitale, alors il suffit d'écrire une méthode tel quel : 

```java
@Check
public void checkNameStartsWithCapital(Entity entity) {
    if (!Character.isUpperCase(entity.getName().charAt(0))) {
        warning("Name should start with a capital", 
            entity.eContainingFeature());
    }
}
```

On spécifie la méthode qui doit être appelé grâce à l'annotation `@Check` et on renvoie les erreurs (respectivement avertissements) via la méthode `error` (respectivement `warning`) avec comme argument le message d'erreur et la *feature* (attribut d'une des règles de la syntaxe) pour spécifier ce qui renvoie l'erreur.

Dans notre cas, une seule méthode est rajoutée `checkType`. À condition qu'il n'y pas d'erreur de syntaxe, elle appele le typage (comme expliqué dans la section [Gestion des erreurs de type](#Gestion-des-erreurs-de-type)) et renvoie ces erreurs. 

Note : pour plus de détails, se référer à la documentation officielle (pas totalement à jour) : [Official Xtext documentation - Validation](https://eclipse.dev/Xtext/documentation/303_runtime_concepts.html#validation)

### Scoping personnalisé
*cf `fr.enseeiht.ocl.xtext.scoping.OclScopeProvider` in `fr.enseeiht.ocl.xtext` project*

Lors d'appel (*VariableExp*) à une variable ou un attribut (*Auxiliary*) dans la syntaxe, Xtext s'occupe de faire le lien automatique avec l'élément correspondant:
```=
Auxiliary : Iterator|LocalVariable|Parameter|Attribute;
VariableExp:
	referredVariable=[Auxiliary]
```

Lors de la résolution de cette règle, Xtext va lier la variable `referredVariable` avec l'objet correspondant.

Cependant par défaut Xtext ne prend pas en compte le masquage, et autorise de référencer une variable peu importe où elle est déclarée. Par exemple, par défaut on aurait le droit d'écrire ca : 
```=
inv test:
  (let i = 1 in true) and (i = 2)
```

Ainsi, une *VariableExp* ne doit pouvoir référencer que des *Auxiliary* présent dans ses parents, des *FeatureDef* présents dans son *ContextBlock* ou des *FeatureDef* présents dans son *Module*

Pour avoir ce comportement, il faut modifier la classe *OclScopeProvider*.

La classe *Scoper*, quant à elle, permet d'associer une valeur *Object* à un *Auxiliary*. *Scoper* est basiquement un wraper autour d'un *Map<Auxiliary, Object>.*

Note: seuls les *Parameter*, *LocalVariable* et *Iterator* devraient être présents dans *Scoper*. Les *Attribute* ne sont en effet pas des variables. Il serait même préférable de les séparer au niveau de la syntaxe si possible.

### Auto-Complétion
*cf `fr.enseeiht.ocl.xtext.ui.contentassist.OclProposalProvider` in `fr.enseeiht.ocl.xtext.ui` project*

<img src="https://hedgedoc.inpt.fr/uploads/9fb223a3-c099-48e7-8363-af37835b0061.png" alt="drawing" width="400"/>

Xtext implement le rajout de proposition d'auto-complétion pour chaque feature de chaque éléments. Pour cela, il faut `Override` la méthode correspondante `complete{ElementName}_{FeatureName}` dans la classe *OclProposalProvider*. Cette méthode est appelé à chaque fois qu'un utilisateur appuis sur `Ctrl-Espace` alors qu'il essaye de remplir la feature correspondante de l'élement correspondant.

Il suffit ensuite d'appeler l'instruction suivante : 
```java
acceptor.accept(createCompletionProposal(proposal, context));
```
Avec comme argument proposal la chaine de caractère a proposer (si plusieurs chaines de caractères souhaite être proposé, appelez plusieurs fois la méthode)

Note : si la syntaxe est ambigue, alors la méthode appelé sera le *container* le plus proche qui est sur.

Il existe aussi 3 méthodes supplémentaires qui sont redéfini :
```java
@Override
protected boolean doCreateIntProposals() {
    return false;
}

@Override
protected boolean doCreateStringProposals() {
    return false;
}

@Override   
protected boolean doCreateIdProposals() {
    return false;
}
```


Celles-ci servent d'enlever des propositions par défaut en fonction de si l'élément à proposer était un Int, un String, ou un ID. Ces propositions étant :
- **`stringSymbol`** pour un String.
- **`1`** pour un Int.
- **`Name`** pour un ID.

Il a été décidé de les enlever pour plus de clarté.

Note : pour plus de détails, se référer à la documentation officielle (pas totalement à jour) : [Official Xtext documentation - Content Assist](https://eclipse.dev/Xtext/documentation/310_eclipse_support.html#content-assist)

### Outline
*cf `fr.enseeiht.ocl.xtext.ui.outline.OclOutlineTreeProvider` in `fr.enseeiht.ocl.xtext.ui` project*

La vue *Outline* est une des vue pour aider à l'écriture des mocl, elle sert en particulier à l'utilisateur pour éviter de se tromper sur les prioritées (ex : `false and true = false` $\neq$ `(false and true) = false`). Ainsi la vue *Outline* va représenter chaque élément comme un noeud avec des enfants. Par exemple le noeud `+` pourra contenir les élements `1` et un autre noeud `*` contenant lui même `2` et `3` (-> `1 + 2*3`).

<p align="center">
  <img src="https://hedgedoc.inpt.fr/uploads/a023b3ee-4997-46e2-bed6-38b656a939cb.png" width="300" />
  <img src="https://hedgedoc.inpt.fr/uploads/1736516a-c5fd-46e2-b08f-bf28af941460.png" width="400" />
</p>

Il y a 2 modifications possible à ce niveau : 

1. le nom du noeud, XText essaye de trouver le nom par lui même, mais il peut échouer, si c'est le cas, il suffit de modifier la méthode `getOutlineString` de l'éléement correspondant au noeud. Par exemple pour renvoyer la string "null" pour l'élément *NullLiteralExp*, il suffit de modifier la méthode de la classe *NullLiteralExpValidationAdapter* tel quel :
    
    ```
      /**
       * Return the string visible in the outline
       * @return outline name
       * @generated NOT
       */
       @Override
      public String getOutlineString() {
        return "null";
      }
    ```
2. les enfants des noeuds, afin d'éviter l'afficahge d'éléments superflu, il est possible d'outrepasser les enfants d'un noeud, pour cela il faut modifier `_createChildren` de la classe *OclOutlineTreeProvider*, et appeler la méthode `_createNode` avec en argument les nouveaux éléments.

Note : pour plus de détails, se référer à la documentation officielle (pas totalement à jour) : [Official Xtext documentation - Outline](https://www.eclipse.org/Xtext/documentation/310_eclipse_support.html#outline)

## Architecture de tests

Une architecture de tests complète a été créé pour ce projet, elle est composé de plusieurs projets :
- `fr.enseeiht.ocl.testsLauncher` qui correspond au projet principal
- `TestsUnitaires` qui contient tout les tests unitaires
- Plusieurs autre projet qui sont sous la forme `{NomDuMétamodèle}-{NomDuCréateur}-{OK|KO}`, ils correspondent à des tests complets récupéré de nos projets personnels d'IDM

### Structure

Le projet principal est composé de :
- La classe `fr.enseeiht.ocl.testsLauncher.util.LauncherUtils` qui s'occupe de vérifier la syntaxe, le type et la validation de chaque test. C'est elle qui appelle tout les composants nécessaire pour simuler l'éxecution d'un MOCL comme si c'était dans l'éclipse de déploiement.
- Des exceptions situé dans `fr.enseeiht.ocl.testsLauncher.exceptions` qui pourront être remonté par la classe *LauncherUtils*.
- Le package `fr.enseeiht.ocl.testsLauncher.test` contenant les classes faisant les tests complets (`ok.ayoub.PetrinetTest`; etc...) et les tests unitaires (`TestsUnitaires`)
- La classe `fr.enseeiht.ocl.testsLauncher.TestSuite` qui est vide mais qui permet de rassembler tout les tests présent dans le package `fr.enseeiht.ocl.testsLauncher.test` grâce à l'annotation `@SelectPackages("fr.enseeiht.ocl.testsLauncher.test")` permettant de tous les exécuter en même temps.

### Lancement des tests

Lancer les tests se fait donc naturalement en exécutant la classe `fr.enseeiht.ocl.testsLauncher.TestSuite` du projet `fr.enseeiht.ocl.testsLauncher` avec JUnit 5, cela lancera une vue arborescente avec tout les tests.

Si vous souhaitez lancer un test précis (pour éviter de relancer tout les tests), vous pouvez a partir de cette vue arborescente, faire un click droit "Run", ce qui lancera uniquement ce test.

### Tests Unitaires

#### Structure JUnit

*cf `fr.enseeiht.ocl.testsLauncher.test.TestsUnitaires`*

Un test JUnit correspond à un type de test :
- Invalide à caude de la syntaxe -> Vérifie que l'erreur *SyntaxException* a été renvoyé.
- Invalide à caude du typage -> Vérifie que l'erreur *CheckTypeException* a été renvoyé.
- Invalide à caude de la validation (retourne `null`) -> Vérifie que `null` est renvoyé.
- Invalide à caude de la validation -> Vérifie que `false` est renvoyé.
- Valide -> Vérifie que `true` est renvoyé.

Ces tests sont paramétrisées par l'ensemble des fichiers de tests leur correspondant. La section [Ecriture de test unitaire](#Ecriture-de-test-unitaire) explique comment savoir quels fichiers sont liée à quelle catégorie.

#### Ecriture de test unitaire

Pour l'écriture de tests unitaires, veuillez vous retournez vers l'aide présent sur le github : [README-TEST.md](https://github.com/guilhemmgt/yet-another-ocl-interpretor/blob/main/README-TEST.md).

## Trucs et Astuces

### Ajouter un type

Pour ce tutoriel, imaginons que nous voulons implémenter le type *UnlimitedIntegers* (qui correspond aux entiers positifs avec la valeur \*, invalide, qui représente l'infini).

1. Ajouter le type, sa déclaration, ses litéraux de valeur et de type dans la syntaxe.
2. Créer une classe dans `fr.enseeiht.ocl.xtext.types` qui implémente OclType. Ensuite, puisque notre type est un sous-type des entiers (*OclInteger*), on la fait hériter de la classe correspondante. 
3. Implémenter le constructeur si besoin (pas le cas ici), puis définir la conformance et l'unifiaction. Notre type se conforme à *OclInteger*, par extention à *OclReal*, et enfin à *OclAny* par définition. Attention, prendre en compte dans l'unification que `this.unifyWith(oclVoid)` donne toujours `this` et que `this.unifyWith(oclInvalid)` donne toujours `oclInvalid` (l'objet, pas la classe ! Sinon on perd le message d'erreur).
4. Générer le Xtext : clique droit sur `fr.enseeiht.ocl.xtext.GenerateOcl/mwe2`/*run as*/*MWE2 Workflow*.
5. Modifier toutes les opérations définies pour ce type si besoin. Si l'addition na pas été substanciellement redéfinie pour *OclInteger*, il faut prendre en compte dans `fr.enseeiht.ocl.xtext.jet-gen.fr.enseeiht.ocl.xtext.ocl.adapter.impl/AddOpCallExpValidationAdapter.java` que `1+* == invalid`.
6. Ne pas oublier d'implémenter le `getType()` des litéraux, des *Classifier* et des autres classes éventuellement générées par la syntaxe.
7. Ajouter toutes les opérations nécessaires, tel que décrit ci-dessous.

Les fichiers générés se trouveront dans `fr.enseeiht.ocl.xtext.jet-gen.fr.enseeiht.ocl.xtext.ocl.adapter.impl`. Il ne faut pas oublier de mettre `not` dans le `@generated` qui se trouve dans le commentaire de documentation du `getType()`, sinon toute modification sera écrasée si le workflow est regénéré.

### Ajouter une opération de base
Pour ajouter une nouvelle opération il faut créer une nouvelle classe qui implémente l'interface *IOclOperation*, et la rajouter dans l'enum *OclOperationEnum*.

Imaginons que nous devions ajouter une nouvelle operation.

1. Creer la classe OclNomOperation
```Java
package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;

public class OclNomOperation implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
        // Cast des arguments et de l'objet source
        [...]
        // Levée d'erreurs si arguments invalides (par exemple : nombre trop grand dans les arguments d'un subString)
        
        // Calcul de la valeur de retour
		return resultValue;
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		// Type de retour, si le type de retour dépend de l'entrée (par ex: operation sur les collections) renvouyer le type le plus précis
        // Si le type source est incorrect (par ex: union(Collection<T>) appliqué sur un String) renvoyer le type le plus général, de toute façon la résolution échouera
        return new OclInteger();
	}

	@Override
	public List<OclType> getArgsType(OclType sourceType, List<OclType> argsType) {
        // Liste des types des operation, dans l'ordre, ici il n'y en a aucun donc liste vide 
		return new ArrayList<OclType>();
	}

	@Override
	public int getArgsAmount() {
        // Nombre d'argument pris par l'operation 0 si aucun
		return 0;
	}

	@Override
	public OclType getSourceType() {
        // Type le plus général sur lequel l'operation peut être  appliquée
		return sourceType;
	}

	@Override
	public String getName() {
        // Nom de l'opération tel qu'utilisé dans le code 
		return "nomOperation";
	}
}
```
Note : Pour les remontées d'erreurs dans le *getValue* se référer à la section [Erreurs à l'interpretation](#Erreurs-à-l’interpretation)

2. Ajouter la classe à *OclOperationEnum*
Ouvrir la classe *OclOperationEnum* et y ajouter la classe au classe corespondant à l'operation *size*
```JAVA
size(Arrays.asList(new OclSize(),new OclSizeString()),
```

Note : pour ajouter une operation qui n'existait pas encore il faut rajouter la ligne suivante avec operationName le nom de l'operation (en respectant la casse) et *OclOperationName* la classe qui implémente l'operation. 
```Java
operationName(Arrays.asList(new OclOperationName())),
```

### Ajouter un itérateur

L'ajout d'itérateurs est très similaire à l'ajout d'opérations. Pour ajouter un nouvel itérateur, il faut créer une nouvelle classe qui implémente l’interface *IOclIterator*, et l'instancier dans l’énumération *OclIteratorEnum*. Prendre exemple sur les itérateurs déjà implémentés.

### Ajouter une règle d'autocomplétion

Pour ajouter une règle d'autocomplétion, il faut rajouter une méthode dans la classe *OclProposalProvider*.

Imaginons que l'on souhaite rajouter les instances d'enum (le `enum1` dans `empty!empty.Enum#enum1`). 

1. Aller checher dans la syntaxe sur quel feature cette auto-complétion s'applique. Ici, c'est dans la règle *EnumLiteralExp* et la feature s'appelle *name* :
    
    ```
    EnumLiteralExp : 
    ecoreTypes=[Import]'!'enum_=[ecore::EEnum|QualifiedName] "#" name=ID
    ```
2. Redéfinir la méthode `complete{ElementName}_{FeatureName}`. Ici, ce sera donc `completeEnumLiteralExp_name` :
    
    ```java
    @Override
    public void completeEnumLiteralExp_Name(EObject model, Assignment assignment,
            ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
        super.completeEnumLiteralExp_Name(model, assignment, context, acceptor);
    }
    ```
3. Il ne manque plus qu'appeler la méthode `accept` du `acceptor` :
    
    ```java
    @Override
    public void completeEnumLiteralExp_Name(EObject model, Assignment assignment,
            ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
        super.completeEnumLiteralExp_Name(model, assignment, context, acceptor);
        
        //S'il n'y a pas d'ambigueté avec la syntaxe, model sera toujours l'élément désiré donc dans notre cas : EnumLiteralExp
        EnumLiteralExp enumLiteralExp = (EnumLiteralExp) model;
        
        // On parcourt toutes les instances de l'enum et on crée une proposition pour chacune d'elles.
        for (EEnumLiteral enumLiteral : enumLiteralExp.getEnum_().getELiterals()) {
            acceptor.accept(createCompletionProposal(enumLiteral.getLiteral(), context));
        }
    }
    ```
    Note : Ne pas oublier le `@Override`








