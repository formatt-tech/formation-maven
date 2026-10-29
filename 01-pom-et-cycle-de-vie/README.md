# 1. Le POM et le cycle de vie

**Durée** : 45 minutes.
**Objectif** : savoir ce que fait vraiment `mvn verify`, d'où viennent les plugins qui travaillent, et où va le jar.

## L'essentiel

Le **POM** (`pom.xml`) décrit le projet : ses **coordonnées** (`groupId:artifactId:version`), ce qu'il produit (`packaging`), ses dépendances. Il ne décrit presque jamais *comment* construire : Maven le sait déjà.

Maven construit en suivant un **cycle de vie**, une suite de **phases** toujours dans le même ordre :

```text
validate → compile → test → package → verify → install → deploy
```

Lancer une phase lance **toutes celles qui la précèdent**. Une phase ne fait rien par elle-même : des **goals** de plugins y sont attachés. Pour un `jar`, Maven attache par défaut :

| Phase | Goal attaché | Ce qu'il fait |
|---|---|---|
| `compile` | `compiler:compile` | compile `src/main/java` dans `target/classes` |
| `test` | `surefire:test` | lance les tests de `src/test/java` |
| `package` | `jar:jar` | assemble `target/<artifactId>-<version>.jar` |
| `install` | `install:install` | copie le jar dans le dépôt local, `~/.m2/repository` |
| `deploy` | `deploy:deploy` | publie le jar dans un dépôt distant |

`clean` appartient à un autre cycle de vie : il vide `target/`. On l'enchaîne au besoin : `mvn clean verify`.

Chaque ligne `--- plugin:version:goal (exécution) @ projet ---` du journal dit quel plugin travaille, dans quelle version, et pourquoi.

## Travaux pratiques

Toutes les commandes se lancent **à la racine du dépôt**. Sous Windows, hors du Dev Container : `.\mvnw.cmd` au lieu de `./mvnw`.

### 1. Les phases s'enchaînent

```bash
./mvnw -f 01-pom-et-cycle-de-vie/exercice validate
```

Le build réussit. Le dossier `exercice/target/` existe-t-il ?

```bash
./mvnw -f 01-pom-et-cycle-de-vie/exercice compile
./mvnw -f 01-pom-et-cycle-de-vie/exercice test
```

Dans le journal de `test`, relevez les lignes `--- … ---`. Pour chacune : quel plugin, quelle version, quel goal ?

```bash
./mvnw -f 01-pom-et-cycle-de-vie/exercice package
```

Quel fichier est apparu dans `target/` ? D'où vient son nom ?

### 2. Le dépôt local

```bash
./mvnw -f 01-pom-et-cycle-de-vie/exercice install
```

Retrouvez le jar dans `~/.m2/repository`. Quel chemin suit-il, et pourquoi celui-là ?

### 3. Le POM effectif

Le `pom.xml` de l'exercice ne déclare aucun plugin. Demandez à Maven le POM qu'il utilise vraiment :

```bash
./mvnw -f 01-pom-et-cycle-de-vie/exercice help:effective-pom -Doutput=target/pom-effectif.xml
```

Ouvrez `exercice/target/pom-effectif.xml`. Quelle version de `maven-surefire-plugin` y trouvez-vous ? Quelle adresse de dépôt ? Ni l'une ni l'autre n'est dans le `pom.xml` : d'où viennent-elles ?

### 4. Une phase, ou un goal seul

```bash
./mvnw -f 01-pom-et-cycle-de-vie/exercice clean jar:jar
```

Le build réussit, avec un avertissement. Lequel ? Ouvrez le jar produit : que contient-il ? Pourquoi est-ce différent de `package` ?

## Réponses

<details>
<summary>1. Les phases s'enchaînent</summary>

- `validate` vérifie seulement que le projet est correct : rien n'est compilé, `target/` n'existe pas.
- `test` affiche, dans l'ordre : `resources:resources`, `compiler:compile`, `resources:testResources`, `compiler:testCompile`, `surefire:test`. Les phases `compile` et `test` ont été rejouées : chaque commande repart du début du cycle.
- Les versions affichées (par exemple `compiler:3.16.0`, `surefire:3.6.0`) n'apparaissent nulle part dans le projet : ce sont celles que choisit la version de Maven utilisée, ici celle du wrapper.
- `package` produit `target/facture-1.0.0-SNAPSHOT.jar` : `artifactId`, tiret, `version`.
</details>

<details>
<summary>2. Le dépôt local</summary>

`~/.m2/repository/io/github/formatttech/formation/maven/facture/1.0.0-SNAPSHOT/facture-1.0.0-SNAPSHOT.jar` : le `groupId` devient une suite de dossiers, puis l'`artifactId`, puis la version. Un autre projet de la machine qui déclare cette dépendance la trouve là.

`install` ne sert qu'à ça : rendre le jar disponible aux autres projets **de la même machine**. Pour vérifier un projet, `verify` suffit.
</details>

<details>
<summary>3. Le POM effectif</summary>

Le POM effectif est le `pom.xml`, complété par ses parents éventuels et par le **super POM**, le POM implicite de Maven. Les versions des plugins du cycle de vie viennent de la version de Maven ; le dépôt `https://repo.maven.apache.org/maven2`, Maven Central, vient du super POM.

En cas de doute sur une configuration, le POM effectif est la référence : il montre ce que Maven exécutera, pas ce qu'on croit avoir écrit.
</details>

<details>
<summary>4. Une phase, ou un goal seul</summary>

Avertissement : `JAR will be empty - no content was marked for inclusion!`. Le jar ne contient que ses métadonnées (`META-INF`), aucune classe.

Un goal lancé seul (`jar:jar`) ne lance que lui : rien n'a compilé, après le `clean`. Une phase (`package`) lance tout le cycle jusqu'à elle. Dans la vie courante, on lance des phases ; un goal seul sert à un outil ponctuel (`help:effective-pom`, `dependency:tree`).
</details>

## À retenir

- On lance une **phase** ; elle entraîne toutes les précédentes.
- `verify` pour vérifier un projet, `install` seulement quand un autre projet de la machine en a besoin.
- Le journal (`--- plugin:version:goal ---`) et le POM effectif disent ce que Maven fait réellement.
- `clean` n'est pas systématique ; il devient nécessaire quand une classe est renommée ou supprimée, car son ancien `.class` reste dans `target/`.
