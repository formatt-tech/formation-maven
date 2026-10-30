# 9. Diagnostiquer un build

**Durée** : 30 minutes.

**À la fin de ce module, vous saurez** :
- lire un échec de Maven : quel plugin, quel goal, quel projet, et pourquoi ;
- choisir l'outil qui confirme la cause, parmi ceux des modules précédents ;
- réparer trois pannes courantes, sans relancer au hasard.

## Le point de départ

Le dossier [`exercice/`](exercice) contient trois projets, et aucun ne se construit :

| Projet | Ce qu'il fait |
|---|---|
| [`conges/`](exercice/conges/pom.xml) | le prochain jour de congé d'un salarié |
| [`import-clients/`](exercice/import-clients/pom.xml) | les clients d'un fichier CSV |
| [`annuaire/`](exercice/annuaire/pom.xml) | la présentation des noms dans l'annuaire |

Le réflexe le plus répandu devant un build cassé : relancer, avec `clean`, puis avec `-U`, puis supprimer `~/.m2`… La cause est presque toujours écrite dans le journal. Ce module apprend à la lire.

## Ce qu'il faut savoir

**Lire un échec, dans l'ordre.**
1. **La ligne `Failed to execute goal`**, en bas du journal. Elle dit qui a échoué, avec les mots du module 1 : `Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.16.0:compile (default-compile) on project conges: Compilation failure`, c'est le plugin `compiler`, en version 3.16.0, son goal `compile`, dans l'exécution `default-compile`, sur le projet `conges`. Sans plugin (`Failed to execute goal on project …`), c'est Maven lui-même, le plus souvent en résolvant les dépendances.
2. **La première erreur**, en remontant le journal. Les suivantes en sont souvent la conséquence.
3. **Le message lui-même**, mot à mot, avec le fichier et la ligne quand il y en a.

**Puis confirmer la cause avec le bon outil**, plutôt que de deviner :

| Symptôme | Outil | Module |
|---|---|---|
| une classe ou un paquet introuvable, une version inattendue | `dependency:tree` | 2 et 3 |
| une configuration dont on ne sait pas d'où elle vient | `help:effective-pom` | 4 |
| un test qui ne tourne pas, ou un échec de test | les rapports dans `target/surefire-reports` | 6 |
| un paramètre de plugin, la propriété qui le règle | `help:describe … -Ddetail` | 6 et 7 |
| un comportement différent en CI | `help:active-profiles`, puis le profil de la CI | 7 |
| un téléchargement qui échoue | `help:effective-settings` | 8 |
| tout le reste | `-e` (la pile d'appels de l'erreur), `-X` (le journal détaillé, très long) | |

**Ce qu'il ne faut pas faire** : relancer sans rien changer, supprimer tout `~/.m2`, ajouter `-U` par réflexe. `-U` force Maven à revérifier les dépôts ; il ne répare rien si la cause est ailleurs.

## Travaux pratiques

Les commandes se lancent à la racine du dépôt. Pour chaque projet : lisez l'échec, trouvez la cause, confirmez-la, réparez.

### 1. `conges`

```bash
./mvnw -f 09-diagnostiquer/exercice/conges verify
```

Quel plugin a échoué, et pendant quel goal ? Quel est le message, et sur quelle ligne de quel fichier ? La méthode existe pourtant : pourquoi le compilateur ne la trouve-t-il pas ? Indice : regardez les propriétés du POM. Il y a deux réparations possibles : lesquelles, et comment choisir ?

### 2. `import-clients`

```bash
./mvnw -f 09-diagnostiquer/exercice/import-clients verify
./mvnw -f 09-diagnostiquer/exercice/import-clients verify
```

Lancez-le deux fois. La ligne `Failed to execute goal` cite-t-elle un plugin ? Les deux messages sont-ils les mêmes ? Relancez avec `-U` : qu'est-ce que ça change ?

Cherchez `commons-csv` sur [Maven Central](https://central.sonatype.com/artifact/org.apache.commons/commons-csv) et réparez.

### 3. `annuaire`

```bash
./mvnw -f 09-diagnostiquer/exercice/annuaire verify
```

Le compilateur dit que le paquet `org.apache.commons.lang3` n'existe pas. La dépendance est pourtant dans le POM. Confirmez la cause avec l'outil du tableau, puis réparez.

### 4. Vos propres pannes

Reprenez la dernière erreur Maven qui vous a bloqué. Avec ce que vous savez maintenant : quelle ligne du journal fallait-il lire en premier, et quel outil aurait confirmé la cause ?

## Réponses

<details>
<summary>1. <code>conges</code></summary>

`Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.16.0:compile (default-compile) on project conges: Compilation failure` : le compilateur, pendant la compilation du code principal. Le message : `Conges.java:[18,48] cannot find symbol`, `symbol: method getFirst()`, `location: variable aVenir of type java.util.List<java.time.LocalDate>`.

`List.getFirst()` est apparue avec Java 21. Le POM fixe `maven.compiler.release` à `17` : le compilateur vérifie alors le code contre l'API de Java 17, même s'il tourne sous Java 25. Le code n'est pas faux, c'est la cible qui ne le permet pas.

Deux réparations, selon le Java sur lequel l'application tourne en production :
- si c'est Java 21 ou plus récent, on monte `maven.compiler.release` ;
- si c'est Java 17, la cible est juste, et c'est le code qu'on adapte : `aVenir.get(0)`.

La question à poser n'est donc pas « comment faire passer le build », mais « quelle est la bonne cible ».
</details>

<details>
<summary>2. <code>import-clients</code></summary>

`Failed to execute goal on project import-clients: Could not resolve dependencies` : aucun plugin, c'est Maven qui n'a pas pu résoudre une dépendance, avant même de compiler. Au premier lancement : `Could not find artifact org.apache.commons:commons-csv:jar:1.41.1 in central`. Au second : `… was not found in https://repo.maven.apache.org/maven2 during a previous attempt. This failure was cached in the local repository and resolution is not reattempted until the update interval of central has elapsed or updates are forced`. Maven a mémorisé l'échec : relancer ne sert à rien. Dans le Codespace, la préparation de l'environnement a déjà fait la première tentative : vous voyez peut-être le second message dès le premier lancement.

`-U` force une nouvelle tentative, et Maven retombe sur `Could not find artifact` : la version n'existe pas. Sur Maven Central, la dernière version est `1.14.1` ; `1.41.1` est une faute de frappe. Avec `1.14.1`, le build passe.

`-U` n'est utile que si l'artefact a été publié **depuis** l'échec mémorisé, par exemple une version que l'équipe voisine vient de déployer sur le dépôt de l'entreprise.
</details>

<details>
<summary>3. <code>annuaire</code></summary>

`dependency:tree` :

```bash
./mvnw -f 09-diagnostiquer/exercice/annuaire dependency:tree
```

affiche `org.apache.commons:commons-lang3:jar:3.21.0:test`. La dépendance est en portée `test` (module 2) : elle n'est visible que des tests, pas du code principal. Elle avait sans doute été ajoutée pour un test, avant que le code principal ne s'en serve. On retire `<scope>test</scope>` ; le build passe, et l'arbre affiche `:compile`.
</details>

<details>
<summary>4. Vos propres pannes</summary>

Pas de réponse unique. Les causes les plus courantes, en entreprise : un miroir ou un mot de passe manquant dans `settings.xml` (module 8), deux versions d'une même bibliothèque (module 3), un projet voisin pas installé ou installé dans une vieille version (module 4), un profil actif en CI et pas sur le poste (module 7), un test jamais lancé (module 6).
</details>

## À retenir

- Lire d'abord **`Failed to execute goal`** (qui a échoué), puis **la première erreur** du journal (pourquoi).
- **Confirmer la cause avec un outil** : `dependency:tree`, `help:effective-pom`, les rapports de tests, `help:describe`, `help:active-profiles`, `help:effective-settings`.
- **Relancer sans rien changer ne répare rien** ; `-U` ne sert que si un artefact a été publié depuis l'échec.
- Avant de réparer, se demander ce qui est juste : le code, ou la configuration.
