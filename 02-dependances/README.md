# 2. Les dépendances

**Durée** : 30 minutes.

**À la fin de ce module, vous saurez** :
- retrouver toutes les bibliothèques d'un projet, y compris celles qu'on n'a pas déclarées, et d'où elles viennent ;
- choisir la bonne portée pour une dépendance ;
- comprendre quelle version Maven retient quand deux versions d'une même bibliothèque se présentent ;
- retirer une bibliothèque indésirable.

## Le point de départ

Ouvrez [`exercice/pom.xml`](exercice/pom.xml). Le projet déclare deux dépendances : `httpclient`, le client HTTP d'Apache, et JUnit pour les tests. Ouvrez ensuite la classe [`AdresseDuService`](exercice/src/main/java/io/github/formatttech/formation/maven/service/AdresseDuService.java) : elle utilise `URIBuilder`, qui vient de `httpclient`, mais aussi `HttpHost`, qui vient d'une bibliothèque que le POM ne mentionne nulle part.

Le projet compile pourtant. D'où vient cette bibliothèque ?

## Ce qu'il faut savoir

**Une dépendance en apporte d'autres.** Chaque bibliothèque publiée sur un dépôt Maven a son propre POM, avec ses propres dépendances. Quand on déclare `httpclient`, Maven lit son POM, et ajoute au projet ce dont `httpclient` a besoin, puis ce dont ces bibliothèques ont besoin, et ainsi de suite. Ce sont les **dépendances transitives**. Un projet qui déclare deux dépendances en utilise souvent une dizaine.

**Chaque dépendance a une portée** (`<scope>`), qui dit quand elle est disponible :

| Portée | Disponible pour | Exemple |
|---|---|---|
| `compile` (par défaut) | le code, les tests, l'exécution | une bibliothèque utilisée par le code |
| `test` | les tests seulement | JUnit |
| `provided` | le code et les tests, mais fournie par l'environnement à l'exécution | l'API Servlet, fournie par le serveur d'applications |
| `runtime` | les tests et l'exécution, pas la compilation du code | un pilote JDBC |

Une dépendance transitive hérite d'une portée calculée à partir des deux : ce qui arrive par une dépendance de test reste en test.

**Une seule version par bibliothèque.** Si deux chemins apportent deux versions d'une même bibliothèque, Maven n'en garde qu'une. Sa règle : **la plus proche du projet gagne**, c'est-à-dire celle qui passe par le moins d'intermédiaires ; à égalité, la première déclarée. Pas la plus récente.

Pour voir tout cela, un outil : `dependency:tree`, qui affiche l'arbre complet des dépendances du projet.

## Travaux pratiques

Les commandes se lancent à la racine du dépôt, comme au module 1.

### 1. L'arbre des dépendances

```bash
./mvnw -f 02-dependances/exercice dependency:tree
```

Le journal affiche un arbre. La première ligne est le projet ; chaque niveau d'indentation est une dépendance apportée par la ligne au-dessus. Chaque ligne se lit `groupId:artifactId:type:version:portée`.

Les signes en début de ligne ne servent qu'à dessiner l'arbre :

| Signe | Signification |
|---|---|
| `+-` | une dépendance, suivie d'autres au même niveau |
| `\-` | la **dernière** dépendance de ce niveau |
| `\|` | la branche du dessus continue plus bas ; ce n'est pas une dépendance |

```text
appel-service
+- httpclient            ← dépendance du projet, d'autres suivent
|  +- httpcore           ← apportée par httpclient
|  +- commons-logging
|  \- commons-codec      ← dernière apportée par httpclient
\- junit-jupiter         ← dernière dépendance du projet
```

Quelles bibliothèques sont arrivées sans être déclarées dans le POM ? Laquelle fournit `HttpHost` ? Quelle portée ont les bibliothèques apportées par JUnit ?

### 2. Une portée qui protège

Dans `AdresseDuService.java`, ajoutez cette ligne au début de la méthode `rechercheParNom` :

```java
org.junit.jupiter.api.Assertions.assertNotNull(machine);
```

```bash
./mvnw -f 02-dependances/exercice compile
```

Quelle erreur obtenez-vous, et pourquoi ? Retirez ensuite la ligne.

### 3. Ce qu'on utilise sans le déclarer

```bash
./mvnw -f 02-dependances/exercice dependency:analyze
```

`dependency:analyze` compare ce que le code utilise vraiment à ce que le POM déclare. Que signale-t-il dans `Used undeclared dependencies` ? Pourquoi est-ce un risque ? Et que penser de ce qu'il signale pour JUnit ?

### 4. Deux versions d'une même bibliothèque

Dans l'arbre de l'exercice 1, `httpclient` apporte `commons-codec` en version 1.11. Ajoutez dans le POM, avant JUnit, une dépendance directe à une version plus ancienne :

```xml
<dependency>
  <groupId>commons-codec</groupId>
  <artifactId>commons-codec</artifactId>
  <version>1.9</version>
</dependency>
```

```bash
./mvnw -f 02-dependances/exercice dependency:tree -Dverbose
```

L'option `-Dverbose` affiche aussi les versions écartées, entre parenthèses. Quelle version de `commons-codec` est retenue ? Pourquoi celle-là, alors que `httpclient` a été construit avec la 1.11 ? Retirez ensuite cette dépendance.

### 5. Retirer une dépendance transitive

Le projet ne veut pas de `commons-logging`, apporté par `httpclient`. Dans le POM, ajoutez à la dépendance `httpclient` :

```xml
<exclusions>
  <exclusion>
    <groupId>commons-logging</groupId>
    <artifactId>commons-logging</artifactId>
  </exclusion>
</exclusions>
```

```bash
./mvnw -f 02-dependances/exercice verify dependency:tree
```

`commons-logging` a-t-il disparu de l'arbre ? Le build passe-t-il ? Est-ce une garantie que l'application fonctionnera ?

## Réponses

<details>
<summary>1. L'arbre des dépendances</summary>

`httpclient` a apporté `httpcore`, `commons-logging` et `commons-codec` ; JUnit a apporté une dizaine de bibliothèques (`junit-jupiter-api`, `junit-jupiter-engine`, `opentest4j`…). `HttpHost` vient de `httpcore`.

Tout ce qu'apporte JUnit est en portée `test` : une portée `test` se transmet à toute la branche.
</details>

<details>
<summary>2. Une portée qui protège</summary>

`package org.junit.jupiter.api does not exist` : JUnit est en portée `test`, il n'existe pas pour le code de `src/main/java`. C'est voulu : du code de test ne peut pas se glisser dans ce qui sera livré.
</details>

<details>
<summary>3. Ce qu'on utilise sans le déclarer</summary>

`httpcore` : le code l'utilise directement (`HttpHost`), mais ne le déclare pas. Il ne doit sa présence qu'à `httpclient`. Le jour où une nouvelle version de `httpclient` n'en dépendra plus, ou en apportera une autre version, le projet cassera sans avoir changé. La règle : **on déclare ce qu'on utilise directement**.

Pour JUnit, l'analyse signale `junit-jupiter-api` comme utilisé sans être déclaré, et `junit-jupiter` comme déclaré sans être utilisé. C'est un faux positif : `junit-jupiter` est un agrégat, une dépendance qui sert seulement à en apporter d'autres, et il est fait pour être déclaré ainsi. `dependency:analyze` est un guide à interpréter, pas un juge.
</details>

<details>
<summary>4. Deux versions d'une même bibliothèque</summary>

La 1.9 est retenue, et l'arbre l'indique : `commons-codec:jar:1.11 … omitted for conflict with 1.9`. Elle est déclarée directement, donc plus proche du projet que la 1.11, qui passe par `httpclient`.

Maven garde **la plus proche, pas la plus récente**. Ici le build passe, parce que le code n'utilise pas `commons-codec`. Mais si `httpclient` appelait une méthode apparue après la 1.9, l'erreur n'apparaîtrait qu'à l'exécution (`NoSuchMethodError`). Le module 3 montre comment imposer une version proprement.
</details>

<details>
<summary>5. Retirer une dépendance transitive</summary>

Oui, `commons-logging` a disparu de l'arbre, et le build passe : le test n'utilise pas les journaux de `httpclient`.

Ce n'est pas une garantie : si `httpclient` appelle `commons-logging` à l'exécution, l'application s'arrête sur une classe introuvable (`NoClassDefFoundError`). Une exclusion se complète presque toujours par un remplaçant, ici le pont `jcl-over-slf4j`, qui redirige ces appels vers SLF4J.
</details>

## À retenir

- `dependency:tree` montre toutes les bibliothèques du projet et le chemin qui les apporte ; `-Dverbose` montre aussi les versions écartées.
- La portée dit quand une dépendance est disponible ; `test` protège le code livré.
- **On déclare ce qu'on utilise directement**, même si une autre dépendance l'apporte déjà.
- En cas de conflit, Maven garde **la version la plus proche**, pas la plus récente.
- Une exclusion retire une bibliothèque ; si quelque chose l'utilise encore, il faut un remplaçant.
