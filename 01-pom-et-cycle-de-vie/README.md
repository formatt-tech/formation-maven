# 1. Le POM et le cycle de vie

**Durée** : 45 minutes.

**À la fin de ce module, vous saurez** :
- ce que Maven fait quand on lance `mvn verify` ;
- lire le journal de Maven pour savoir quel outil a travaillé, et dans quelle version ;
- où Maven range ce qu'il produit.

## Le point de départ

Ouvrez le fichier [`exercice/pom.xml`](exercice/pom.xml). C'est le **POM** (*Project Object Model*), la fiche d'identité du projet. Il dit :
- **qui est le projet** : ses coordonnées, `groupId` (l'organisation), `artifactId` (le nom du projet) et `version` ;
- **ce qu'il produit** : `packaging`, ici un `jar` ;
- **ce dont il a besoin** : ses dépendances, ici JUnit pour les tests.

Il ne dit nulle part comment compiler, où trouver les sources, ni comment lancer les tests. Et pourtant, le projet se construit. Comment Maven sait-il quoi faire ?

## Ce que Maven sait déjà

Maven repose sur des **conventions** : tant qu'un projet les suit, il n'a rien à déclarer.

**Une arborescence convenue.** Les sources sont dans `src/main/java`, les tests dans `src/test/java`, et tout ce que Maven produit va dans `target/`.

**Un déroulé convenu, le cycle de vie.** Construire un projet, c'est toujours la même suite d'étapes, appelées **phases**, dans le même ordre :

```text
validate → compile → test → package → verify → install → deploy
```

Quand on demande une phase à Maven, il exécute **toutes les phases qui la précèdent**, puis celle-là. `mvn package` vérifie, compile, teste, puis assemble le jar.

**Des outils attachés à chaque phase.** Une phase n'est qu'une étape du déroulé : le travail est fait par des **plugins**. Chaque plugin propose des actions, appelées **goals**. Pour un projet qui produit un jar, Maven attache d'office un goal à chaque phase :

| Phase | Goal attaché | Ce qu'il fait |
|---|---|---|
| `compile` | `compile` du plugin `compiler` | compile `src/main/java` dans `target/classes` |
| `test` | `test` du plugin `surefire` | lance les tests de `src/test/java` |
| `package` | `jar` du plugin `jar` | assemble le jar dans `target/` |
| `install` | `install` du plugin `install` | copie le jar dans le dépôt local de la machine |
| `deploy` | `deploy` du plugin `deploy` | publie le jar dans un dépôt partagé |

Enfin, `clean` est à part : il vide `target/`. On le place avant une phase quand on veut repartir de zéro : `mvn clean verify`.

## Travaux pratiques

Vous allez lancer Maven sur le projet de l'exercice, et observer ce qu'il fait dans son **journal**, c'est-à-dire ce qu'il affiche dans le terminal pendant le build.

Toutes les commandes se lancent **à la racine du dépôt**. Le dépôt fournit Maven : on l'appelle par `./mvnw`, ou `.\mvnw.cmd` sous Windows en dehors du Dev Container. L'option `-f` indique le projet à construire.

### 1. Lire le journal

```bash
./mvnw -f 01-pom-et-cycle-de-vie/exercice compile
```

Le journal annonce chaque étape par une ligne de ce genre :

```text
[INFO] --- compiler:3.16.0:compile (default-compile) @ facture ---
```

Elle se lit ainsi :

| Partie | Signification |
|---|---|
| `compiler` | le plugin qui travaille |
| `3.16.0` | sa version |
| `compile` | le goal exécuté |
| `default-compile` | le nom de l'exécution ; `default-…` signale un goal attaché d'office par Maven |
| `@ facture` | le projet concerné, par son `artifactId` |

Quelles lignes de ce genre voyez-vous ? Quel dossier est apparu dans `exercice/target/` ?

### 2. Les phases s'enchaînent

```bash
./mvnw -f 01-pom-et-cycle-de-vie/exercice test
```

Relevez les lignes `--- … ---` : la compilation a-t-elle été refaite ? Combien de tests ont été lancés ?

```bash
./mvnw -f 01-pom-et-cycle-de-vie/exercice package
```

Quel fichier est apparu dans `exercice/target/` ? D'où vient son nom ?

### 3. Le dépôt local

```bash
./mvnw -f 01-pom-et-cycle-de-vie/exercice install
```

Maven copie le jar dans son **dépôt local**, le dossier `~/.m2/repository`, où il range aussi toutes les dépendances qu'il télécharge. Retrouvez-y le jar de l'exercice : quel chemin suit-il ?

### 4. Le POM que Maven utilise vraiment

Le `pom.xml` de l'exercice ne nomme aucun plugin. Pourtant, le journal affiche des plugins avec leur version. Demandez à Maven le POM complet qu'il utilise, appelé **POM effectif** :

```bash
./mvnw -f 01-pom-et-cycle-de-vie/exercice help:effective-pom -Doutput=target/pom-effectif.xml
```

Ouvrez `exercice/target/pom-effectif.xml`, et cherchez `maven-surefire-plugin` : quelle version y trouvez-vous ? Cherchez ensuite `<repositories>` : quelle adresse y est indiquée ?

### 5. Une phase, ou un goal seul

On peut aussi demander directement un goal, sans passer par une phase :

```bash
./mvnw -f 01-pom-et-cycle-de-vie/exercice clean jar:jar
```

Le build réussit, avec un avertissement : lequel ? Que contient le jar produit ? Pourquoi est-il différent de celui de l'exercice 2 ?

## Réponses

<details>
<summary>1. Lire le journal</summary>

Deux lignes : `resources:resources`, qui copie les fichiers de `src/main/resources` (il n'y en a pas ici), puis `compiler:compile`. Le dossier `target/classes` contient la classe `Facture` compilée.
</details>

<details>
<summary>2. Les phases s'enchaînent</summary>

Oui : `test` rejoue tout le début du cycle. Le journal montre `resources:resources`, `compiler:compile`, puis `resources:testResources`, `compiler:testCompile` (la compilation des tests) et `surefire:test`, qui lance 2 tests. Maven ne recompile en réalité que ce qui a changé, mais chaque phase demandée repart toujours du début du cycle.

`package` ajoute `jar:jar`, et produit `target/facture-1.0.0-SNAPSHOT.jar` : l'`artifactId`, un tiret, puis la `version`.
</details>

<details>
<summary>3. Le dépôt local</summary>

`~/.m2/repository/io/github/formatttech/formation/maven/facture/1.0.0-SNAPSHOT/facture-1.0.0-SNAPSHOT.jar` : le `groupId` devient une suite de dossiers, puis viennent l'`artifactId` et la version. C'est le même rangement que sur Maven Central, le dépôt public d'où viennent les dépendances.

`install` ne sert qu'à rendre le jar disponible aux autres projets **de la même machine**. Pour vérifier un projet, `verify` suffit.
</details>

<details>
<summary>4. Le POM que Maven utilise vraiment</summary>

Une version précise de `maven-surefire-plugin` (3.6.0 avec le Maven de ce dépôt), et l'adresse `https://repo.maven.apache.org/maven2`, celle de Maven Central. Ni l'une ni l'autre n'est dans `pom.xml` : elles viennent de Maven lui-même, qui complète chaque POM par ses valeurs par défaut. C'est pourquoi le projet se construit sans rien déclarer.

Quand on se demande ce que Maven va faire, c'est le POM effectif qu'il faut lire, pas seulement `pom.xml`.
</details>

<details>
<summary>5. Une phase, ou un goal seul</summary>

L'avertissement : `JAR will be empty - no content was marked for inclusion!`. Le jar ne contient que des informations sur le projet (dossier `META-INF`), et aucune classe.

Un goal demandé seul ne lance que lui. Après le `clean`, rien n'avait été compilé : `jar:jar` a assemblé un jar vide. Une phase, au contraire, lance tout le cycle jusqu'à elle. Au quotidien, on lance des phases ; un goal seul sert à un besoin ponctuel, comme `help:effective-pom`.
</details>

## À retenir

- Le POM décrit **ce qu'est** le projet ; Maven sait **comment** le construire, par ses conventions.
- On lance une **phase**, et elle entraîne toutes les précédentes. `verify` pour vérifier un projet ; `install` seulement si un autre projet de la machine en a besoin.
- Le **journal** dit quel plugin travaille et dans quelle version ; le **POM effectif** dit tout ce que Maven utilise, valeurs par défaut comprises.
- `clean` n'est pas systématique ; il sert quand une classe a été renommée ou supprimée, car son ancienne version compilée reste dans `target/`.
