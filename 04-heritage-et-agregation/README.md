# 4. Héritage et agrégation

**Durée** : 30 minutes.

**À la fin de ce module, vous saurez** :
- partager une configuration entre plusieurs projets avec un POM parent ;
- construire un projet qui dépend d'un autre projet de l'entreprise ;
- construire plusieurs projets ensemble, dans le bon ordre, avec un POM agrégateur ;
- ne construire qu'une partie d'un ensemble, sans oublier ce dont elle dépend.

## Le point de départ

Le dossier [`exercice/`](exercice) contient trois projets d'une même entreprise :

| Projet | Rôle |
|---|---|
| [`parent/`](exercice/parent/pom.xml) | la configuration commune : version de Java, encodage, JUnit et sa version ; il ne contient pas de code |
| [`facturation/`](exercice/facturation/pom.xml) | les factures, et la règle qui dit si une facture est échue |
| [`relances/`](exercice/relances/pom.xml) | choisit les factures à relancer ; il utilise `facturation` |

Ouvrez les POM de `facturation` et `relances` : ils sont presque vides. Ni version de Java, ni JUnit, ni même `groupId` ou `version`. Tout vient du parent. Et pourtant, `relances` ne se construit pas.

## Ce qu'il faut savoir

**L'héritage : un parent partage sa configuration.** Un projet déclare son parent dans `<parent>`, et reçoit de lui ses propriétés, ses dépendances, son `<dependencyManagement>`, sa configuration de plugins, son `groupId` et sa version. `<relativePath>` dit où trouver le POM du parent sur le disque ; sinon, Maven le cherche dans les dépôts.

**L'agrégation : un agrégateur construit ensemble.** Un POM de `packaging` `pom` qui liste des projets dans `<modules>` les construit tous d'une seule commande. Maven calcule l'ordre à partir des dépendances entre eux : c'est le **reactor**. Et pendant ce build, un projet trouve les autres directement, sans qu'ils soient installés.

**Ce sont deux choses distinctes.** Le parent dit **comment** construire ; l'agrégateur dit **quoi** construire ensemble. Dans beaucoup de projets, un même POM racine joue les deux rôles, d'où la confusion. Mais un projet peut avoir un parent sans faire partie d'aucun agrégat, comme ici.

## Travaux pratiques

Les commandes se lancent à la racine du dépôt.

### 1. Ce qu'un projet hérite

```bash
./mvnw -f 04-heritage-et-agregation/exercice/facturation verify
./mvnw -f 04-heritage-et-agregation/exercice/facturation help:effective-pom -Doutput=target/pom-effectif.xml
```

Le build passe. Dans `facturation/target/pom-effectif.xml`, retrouvez le `groupId` du projet, la propriété `maven.compiler.release`, et la version de `junit-jupiter`. Aucun n'est dans le POM de `facturation` : d'où vient chacun ?

### 2. Un projet qui dépend d'un autre

```bash
./mvnw -f 04-heritage-et-agregation/exercice/relances verify
```

Le build échoue. Lisez le message : que cherche Maven, et où ? Installez `facturation` dans le dépôt local, puis réessayez :

```bash
./mvnw -f 04-heritage-et-agregation/exercice/facturation install
./mvnw -f 04-heritage-et-agregation/exercice/relances verify
```

Il échoue encore, avec un autre message : lequel ? Installez ce qui manque, et réessayez.

### 3. Construire ensemble

Créez le fichier `04-heritage-et-agregation/exercice/pom.xml` :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>

  <groupId>io.github.formatttech.formation.maven</groupId>
  <artifactId>tous-les-projets</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>pom</packaging>

  <modules>
    <module>relances</module>
    <module>facturation</module>
    <module>parent</module>
  </modules>
</project>
```

Les modules sont volontairement dans le mauvais ordre. Lancez :

```bash
./mvnw -f 04-heritage-et-agregation/exercice verify
```

Le journal commence par `Reactor Build Order` : dans quel ordre Maven construit-il les projets, et pourquoi ? Avez-vous eu besoin d'installer quoi que ce soit ?

### 4. Construire une partie seulement

L'option `-pl` (*projects list*) limite le build aux projets nommés :

```bash
./mvnw -f 04-heritage-et-agregation/exercice verify -pl relances
```

Le build passe-t-il ? Avec quelle version de `facturation` les tests de `relances` ont-ils tourné ? Lancez maintenant :

```bash
./mvnw -f 04-heritage-et-agregation/exercice verify -pl relances -am
```

Quels projets sont construits ? Que fait `-am` (*also make*) ?

## Réponses

<details>
<summary>1. Ce qu'un projet hérite</summary>

Le `groupId` et la version, `maven.compiler.release` et la dépendance `junit-jupiter` viennent du POM `parent` ; la version 6.1.3 de JUnit vient du BOM de JUnit que le parent importe (module 3). Le POM effectif montre aussi, en tête, le `<parent>` d'où tout cela provient.
</details>

<details>
<summary>2. Un projet qui dépend d'un autre</summary>

Premier échec : `Could not find artifact …:facturation:jar:1.0.0-SNAPSHOT`. Construit seul, `relances` cherche le jar de `facturation` dans le dépôt local, puis sur Maven Central, comme n'importe quelle dépendance. Il n'y est pas.

Après l'installation de `facturation`, second échec : `Failed to read artifact descriptor for …:facturation`, puis `…:parent:pom:1.0.0-SNAPSHOT (absent)`. Pour utiliser le jar de `facturation`, Maven lit son POM, qui déclare un parent ; et ce parent doit lui aussi être dans le dépôt. `<relativePath>` ne sert qu'au projet qu'on construit, jamais à une dépendance.

Il faut donc installer `parent`, puis `relances` se construit. Ça marche, mais avec une contrainte : à chaque modification de `facturation`, il faut la réinstaller, sans quoi `relances` utilise l'ancienne version.
</details>

<details>
<summary>3. Construire ensemble</summary>

Ordre : `parent`, `facturation`, `relances`, puis l'agrégateur `tous-les-projets`. Maven ignore l'ordre de `<modules>` : il construit d'abord ce dont les autres dépendent. `parent` est le parent des deux autres, et `relances` dépend de `facturation`.

Aucune installation n'est nécessaire : dans un même build, le reactor fournit directement à `relances` le `facturation` qu'il vient de construire. C'est l'intérêt de l'agrégation.
</details>

<details>
<summary>4. Construire une partie seulement</summary>

Avec `-pl relances` seul, le build passe ici, mais avec le jar de `facturation` installé à l'exercice 2, pas avec son code actuel : Maven ne construit que `relances`, et prend `facturation` dans le dépôt local. Sans cette installation, le build échouerait comme au début de l'exercice 2. Dans les deux cas, ce n'est pas ce qu'on veut.

Avec `-pl relances -am`, Maven construit aussi tout ce dont `relances` a besoin dans l'agrégat : `parent`, `facturation`, puis `relances`. C'est la bonne façon de construire une partie d'un ensemble.
</details>

## À retenir

- Le **parent** partage la configuration ; l'**agrégateur** construit ensemble. Un même POM peut jouer les deux rôles, mais ce sont deux mécanismes.
- Un projet construit seul trouve ses dépendances **dans les dépôts**, jamais sur le disque à côté ; et le POM d'une dépendance installée doit pouvoir trouver son parent dans le dépôt.
- Dans un agrégat, le **reactor** calcule l'ordre et partage ce qu'il construit : pas besoin d'`install`.
- Pour construire une partie : `-pl <projet> -am`. Sans `-am`, Maven prend le reste dans le dépôt local, tel qu'il a été installé la dernière fois.
