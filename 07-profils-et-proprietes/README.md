# 7. Profils et propriétés

**Durée** : 35 minutes.

**À la fin de ce module, vous saurez** :
- d'où vient la valeur d'une propriété, et la changer pour un build sans toucher au POM ;
- trouver la propriété qui désactive un contrôle, et quand s'en servir ;
- reproduire sur son poste le build de l'intégration continue ;
- activer un profil automatiquement, et savoir lesquels sont actifs.

## Le point de départ

Le projet [`exercice/`](exercice) présente le relevé d'un compte bancaire, à la française. Ouvrez son [`pom.xml`](exercice/pom.xml) :
- une propriété `java.version.minimale`, utilisée par Enforcer (module 5) pour vérifier la version de Java ;
- une section `<profiles>` avec un profil `ci` : le build de l'intégration continue, plus strict.

```bash
./mvnw -f 07-profils-et-proprietes/exercice verify
```

Le build passe sur votre poste. Il échouerait pourtant en intégration continue : pourquoi, et comment le savoir avant de pousser ?

## Ce qu'il faut savoir

**Une propriété est une valeur nommée, utilisable partout dans le POM** sous la forme `${nom}`. Elle se déclare dans `<properties>`. Certaines sont lues directement par les plugins, comme `maven.compiler.release` ou `project.build.sourceEncoding`.

**La ligne de commande l'emporte sur le POM.** `-Dnom=valeur` remplace, pour ce build seulement, la valeur écrite dans le POM. C'est ce qui permet de changer un réglage sans modifier le projet.

**Beaucoup de paramètres de plugins répondent à une propriété.** `-DskipTests` (module 6) en est un exemple : le paramètre `skipTests` de Surefire répond à la propriété `skipTests`. La description d'un plugin (`help:describe … -Ddetail`) donne, pour chaque paramètre, sa *User property*. C'est ainsi qu'on désactive un contrôle pour un build, sans le retirer du POM.

**Un profil est un morceau de POM qui ne s'applique que s'il est activé.** Il peut ajouter des propriétés, des dépendances, des plugins ou leur configuration. Il s'active :
- à la demande, par `-P<id>` (et se désactive par `-P '!<id>'`) ;
- automatiquement, par une condition dans `<activation>` : une propriété ou une variable d'environnement présente, un fichier présent, une version de Java, un système d'exploitation ;
- par défaut, avec `<activeByDefault>true</activeByDefault>` : mais un tel profil se désactive dès qu'un autre profil du même POM est activé, d'une façon ou d'une autre.

**Un profil sert à changer la façon de construire, pas ce qu'on livre.** Un profil `ci` plus strict, un profil qui lance des contrôles longs : oui. Un profil `dev` et un profil `prod` qui produisent deux jars différents : non, car le jar testé n'est alors pas celui qu'on livre. La configuration propre à chaque environnement se donne à l'application au démarrage, pas à la construction.

## Travaux pratiques

Les commandes se lancent à la racine du dépôt.

### 1. Changer une propriété pour un build

Lisez la valeur de la propriété, puis lancez le build en la changeant :

```bash
./mvnw -q -f 07-profils-et-proprietes/exercice help:evaluate -Dexpression=java.version.minimale -DforceStdout
./mvnw -f 07-profils-et-proprietes/exercice verify -Djava.version.minimale=99
```

Le build passe-t-il ? À quelle ligne du journal s'arrête-t-il, et que dit le message ? Le POM a-t-il changé ?

### 2. Désactiver un contrôle

Imaginons que la règle d'Enforcer vous bloque, et que vous deviez construire quand même, une fois. Cherchez dans la description du goal `enforce` le paramètre `skip`, et la propriété à laquelle il répond :

```bash
./mvnw -f 07-profils-et-proprietes/exercice help:describe -Dplugin=enforcer -Dgoal=enforce -Ddetail
```

Relancez le build de l'exercice 1 en ajoutant cette propriété. Que dit la ligne qui suit `--- enforcer … ---` ?

### 3. Reproduire le build de l'intégration continue

Lancez le build avec le profil `ci`, puis demandez à Maven quels profils sont actifs :

```bash
./mvnw -f 07-profils-et-proprietes/exercice clean verify -Pci
./mvnw -f 07-profils-et-proprietes/exercice help:active-profiles -Pci
```

Pourquoi le build échoue-t-il avec `-Pci`, et pas sans ? Lisez la configuration du profil dans le POM. Corrigez [`Releve.java`](exercice/src/main/java/io/github/formatttech/formation/maven/releve/Releve.java) pour que le build passe avec le profil.

### 4. Activer un profil automatiquement

Les outils d'intégration continue (GitHub Actions, GitLab CI…) définissent la variable d'environnement `CI`. Dans le profil `ci`, après `<id>ci</id>`, ajoutez :

```xml
<activation>
  <property>
    <name>env.CI</name>
  </property>
</activation>
```

Puis lancez, sans `-P` :

```bash
./mvnw -f 07-profils-et-proprietes/exercice help:active-profiles
CI=true ./mvnw -f 07-profils-et-proprietes/exercice help:active-profiles
CI=true ./mvnw -f 07-profils-et-proprietes/exercice help:active-profiles -P '!ci'
CI=false ./mvnw -f 07-profils-et-proprietes/exercice help:active-profiles
```

`CI=true` devant la commande définit la variable pour cette commande seulement (sous PowerShell, hors Dev Container : `$env:CI='true'` avant la commande, puis `Remove-Item env:CI` pour la retirer). Quel profil est actif dans chaque cas ? Le dernier résultat vous surprend-il ?

### 5. Le profil actif par défaut

Ajoutez un second profil, au début de `<profiles>` :

```xml
<profile>
  <id>poste</id>
  <activation>
    <activeByDefault>true</activeByDefault>
  </activation>
</profile>
```

```bash
./mvnw -f 07-profils-et-proprietes/exercice help:active-profiles
CI=true ./mvnw -f 07-profils-et-proprietes/exercice help:active-profiles
```

Quels profils sont actifs dans chaque cas ?

## Réponses

<details>
<summary>1. Changer une propriété pour un build</summary>

`help:evaluate` affiche `25`. Avec `-Djava.version.minimale=99`, le build s'arrête dès `--- enforcer:3.6.3:enforce (verifier-java) ---` : `Detected JDK … is version 25… which is not in the allowed range [99,).` La valeur de la ligne de commande a remplacé celle du POM dans la règle, et le POM n'a pas changé.
</details>

<details>
<summary>2. Désactiver un contrôle</summary>

Le paramètre `skip` répond à `User property: enforcer.skip`. Avec `-Djava.version.minimale=99 -Denforcer.skip`, le journal affiche `Skipping Rule Enforcement.` et le build passe : le plugin s'exécute, mais ne vérifie rien.

Une désactivation se fait ainsi, en ligne de commande, consciemment et pour un build. Écrite dans le POM (`<enforcer.skip>true</enforcer.skip>`), elle désactiverait le contrôle pour tout le monde, CI comprise, et tomberait vite dans l'oubli.
</details>

<details>
<summary>3. Reproduire le build de l'intégration continue</summary>

Le profil `ci` configure le compilateur avec `-Xlint:all` (tous les avertissements, comme au module 5) et `failOnWarning` (un avertissement devient une erreur). Le compilateur signale `Locale(java.lang.String,java.lang.String) in java.util.Locale has been deprecated`, puis `warnings found and -Werror specified`. Sans le profil, la même dépréciation n'est qu'une note du compilateur, et le build passe. `help:active-profiles` confirme : `ci` est actif.

Correction : `private static final Locale FRANCE = Locale.of("fr", "FR");`. Le build passe alors avec `-Pci`.

C'est le réflexe à retenir : quand la CI échoue et pas le poste, on relance sur le poste avec le profil de la CI.
</details>

<details>
<summary>4. Activer un profil automatiquement</summary>

- Sans `CI` : aucun profil actif.
- `CI=true` : `ci` est actif, sans `-P`.
- `CI=true` et `-P '!ci'` : aucun ; la désactivation explicite l'emporte. Les apostrophes empêchent le terminal d'interpréter le `!`.
- `CI=false` : `ci` est **actif**. La condition `<name>` seule teste que la variable **existe**, pas sa valeur. Pour tester la valeur, on ajoute `<value>true</value>` sous `<name>`.

Dans un vrai projet, le profil `ci` s'active ainsi tout seul en intégration continue, et `-Pci` permet de le reproduire sur le poste.
</details>

<details>
<summary>5. Le profil actif par défaut</summary>

Sans `CI`, seul `poste` est actif. Avec `CI=true`, seul `ci` est actif : `poste` a disparu. Un profil `activeByDefault` ne reste actif que tant qu'aucun autre profil du POM n'est activé. Si `poste` portait un réglage nécessaire au build, il serait perdu en CI, sans avertissement. Pour qu'un réglage s'applique toujours, on l'écrit hors des profils.
</details>

## À retenir

- `${nom}` utilise une propriété ; **`-Dnom=valeur` la remplace** pour un build, sans toucher au POM.
- La plupart des contrôles se désactivent par une propriété ; `help:describe … -Ddetail` donne sa *User property*. Une désactivation se fait en ligne de commande, jamais dans le POM.
- Un profil s'active par `-P`, par une condition (`<activation>`), ou par défaut ; **`help:active-profiles`** dit lesquels sont actifs.
- Quand la CI échoue et pas le poste : relancer sur le poste avec le profil de la CI.
- Un profil change la façon de construire, jamais ce qu'on livre ; et `activeByDefault` disparaît dès qu'un autre profil s'active.
