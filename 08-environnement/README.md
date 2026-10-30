# 8. L'environnement : wrapper, dépôt local, `settings.xml`

**Durée** : 30 minutes.

**À la fin de ce module, vous saurez** :
- fixer la version de Maven d'un projet avec le wrapper ;
- où Maven range ce qu'il télécharge, et construire sans réseau ;
- faire passer tous les téléchargements par le dépôt de l'entreprise ;
- ranger chaque réglage au bon endroit : le projet, le poste, ou la ligne de commande.

## Le point de départ

Depuis le début, vous lancez `./mvnw` et non `mvn`, et Maven télécharge plugins et dépendances sur Maven Central. Dans une entreprise, c'est rarement aussi simple : Maven Central est souvent inaccessible, tout passe par un dépôt interne (Nexus, Artifactory…), parfois avec un mot de passe. Et quand deux développeurs n'ont pas la même version de Maven, leurs builds diffèrent (module 5).

Le projet [`exercice/`](exercice) est un petit projet qui utilise `commons-lang3`. Il contient aussi un fichier [`settings-entreprise.xml`](exercice/settings-entreprise.xml), les réglages d'un poste d'entreprise.

## Ce qu'il faut savoir

**Le wrapper fixe la version de Maven.** `mvnw` (et `mvnw.cmd` sous Windows) remplace la commande `mvn`. Il lit `.mvn/wrapper/maven-wrapper.properties`, qui donne la version exacte de Maven (`distributionUrl`), la télécharge une fois dans `~/.m2/wrapper/`, puis l'utilise. Ces trois fichiers se versionnent avec le projet : toute l'équipe et la CI construisent avec le même Maven, donc les mêmes plugins.

**Le dépôt local est le cache de la machine.** Tout ce que Maven télécharge va dans `~/.m2/repository`, et tout ce qu'on installe (`mvn install`, module 4) aussi. Maven n'y retélécharge rien. Avec `-o` (*offline*), il n'utilise que ce cache.

**`settings.xml` décrit le poste, pas le projet.** Il se trouve dans `~/.m2/settings.xml` (l'option `-s` en désigne un autre). On y met ce qui dépend de la machine ou de la personne :
- les **miroirs** : un miroir dont `mirrorOf` vaut `*` intercepte **tous** les téléchargements, plugins compris, et les envoie vers son URL, celle du dépôt de l'entreprise ;
- le **proxy** réseau de l'entreprise ;
- les **identifiants** des dépôts (`<servers>`), jamais en clair : chiffrés par Maven (`mvn --encrypt-password`), ou lus dans une variable d'environnement (`${env.NOM}`).

Rien de tout cela ne va dans un POM : un POM est le même pour tout le monde, versionné, souvent public.

**Chaque réglage a sa place** :

| Où | Ce qu'on y met | Pour qui |
|---|---|---|
| `pom.xml` | ce qu'est le projet et comment le construire | tout le monde |
| `.mvn/` | la version de Maven (`wrapper/`), les options de Maven pour ce projet (`maven.config`, `jvm.config`) | tout le monde |
| `~/.m2/settings.xml` | miroirs, proxy, identifiants | une machine, une personne |
| ligne de commande | `-D`, `-P`, `-o`… | un build |

## Travaux pratiques

Les commandes se lancent à la racine du dépôt.

### 1. Le wrapper du dépôt

```bash
./mvnw -v
cat .mvn/wrapper/maven-wrapper.properties
ls ~/.m2/wrapper/dists
```

Quelle version de Maven le wrapper utilise-t-il, et où l'a-t-il téléchargée ? Essayez aussi `mvn -v` : quel résultat ?

### 2. Ajouter le wrapper à un projet

Le projet de l'exercice n'a pas de wrapper. Ajoutez-lui le sien :

```bash
./mvnw -f 08-environnement/exercice wrapper:wrapper -Dmaven=3.10.0
ls -la 08-environnement/exercice 08-environnement/exercice/.mvn/wrapper
```

Quels fichiers sont apparus ? Lesquels faut-il versionner ? Lancez le build avec le wrapper du projet lui-même :

```bash
cd 08-environnement/exercice
./mvnw verify
cd -
```

### 3. Le dépôt local, et le build sans réseau

```bash
./mvnw -q -f 08-environnement/exercice help:evaluate -Dexpression=settings.localRepository -DforceStdout
ls ~/.m2/repository/org/apache/commons/commons-lang3
./mvnw -f 08-environnement/exercice verify -o
```

Où est le dépôt local ? Quelles versions de `commons-lang3` contient-il ? Le build hors ligne passe-t-il ?

Dans le POM, passez la version de `commons-lang3` à `3.18.0`, et relancez hors ligne. Que dit Maven ? Relancez sans `-o`, puis remettez `3.21.0`.

### 4. Le dépôt de l'entreprise

Ouvrez `settings-entreprise.xml` : un miroir `entreprise`, qui intercepte tout (`*`), vers le dépôt (fictif) d'une entreprise. Lancez le build avec ces réglages, et avec un dépôt local **vide**, pour que Maven doive tout télécharger :

```bash
./mvnw -f 08-environnement/exercice verify -s 08-environnement/exercice/settings-entreprise.xml -Dmaven.repo.local=/tmp/depot-vide
```

Le build passe-t-il ? Quel est le premier fichier que Maven essaie de télécharger, et d'où ? Pourquoi ce fichier-là ?

Remplacez maintenant l'URL du miroir par `https://repo1.maven.org/maven2/` (un autre nom de Maven Central, qui joue ici le rôle d'un dépôt d'entreprise accessible), et relancez la même commande. Puis affichez les réglages que Maven applique réellement :

```bash
./mvnw -f 08-environnement/exercice help:effective-settings -s 08-environnement/exercice/settings-entreprise.xml -Dmaven.repo.local=/tmp/depot-vide
```

Dans les lignes `Downloading from …`, quel nom de dépôt voyez-vous ? Dans les réglages effectifs, combien de miroirs y a-t-il ?

### 5. Chaque réglage à sa place

Pour chacun de ces réglages, où le mettriez-vous : POM, `.mvn/`, `settings.xml` ou ligne de commande ?

1. La version de Java du projet.
2. L'URL du Nexus de l'entreprise.
3. Le mot de passe du Nexus.
4. La version de Maven.
5. Le proxy réseau de l'entreprise.
6. L'activation du profil `ci` pour reproduire la CI (module 7).
7. La version de `commons-lang3`.

## Réponses

<details>
<summary>1. Le wrapper du dépôt</summary>

Maven 3.10.0, comme l'indique `distributionUrl` dans `maven-wrapper.properties`, téléchargé dans `~/.m2/wrapper/dists/apache-maven-3.10.0/…`. Dans le Codespace, `mvn -v` répond `command not found` : Maven n'est pas installé, et n'a pas besoin de l'être. Sur un poste où il l'est, `mvn -v` peut afficher une autre version que `./mvnw -v` : c'est exactement l'écart que le wrapper supprime.
</details>

<details>
<summary>2. Ajouter le wrapper à un projet</summary>

Trois fichiers : `mvnw`, `mvnw.cmd`, et `.mvn/wrapper/maven-wrapper.properties`. Les trois se versionnent. Il n'y a pas de `maven-wrapper.jar` : c'est l'ancien type de wrapper, que l'on rencontre encore dans des projets plus anciens.

Deux précautions quand on les ajoute depuis Windows : `mvnw` doit garder son droit d'exécution (`git add --chmod=+x mvnw`), sinon la CI Linux échoue sur `Permission denied` ; et `mvnw.cmd` doit garder ses fins de ligne Windows (`*.cmd text eol=crlf` dans `.gitattributes`, comme dans ce dépôt).
</details>

<details>
<summary>3. Le dépôt local, et le build sans réseau</summary>

Le dépôt local est `~/.m2/repository` (le dossier personnel de l'utilisateur, suivi de `.m2/repository`). Il contient au moins `3.21.0`, téléchargée à la préparation de l'environnement. Le build hors ligne passe : tout est dans le cache.

Avec `3.18.0` et `-o` : `Cannot access central (https://repo.maven.apache.org/maven2) in offline mode and the artifact org.apache.commons:commons-lang3:jar:3.18.0 has not been downloaded from it before.` Sans `-o`, Maven la télécharge et le build passe. Le mode hors ligne sert en déplacement, ou pour vérifier qu'un build ne dépend plus du réseau.
</details>

<details>
<summary>4. Le dépôt de l'entreprise</summary>

Avec le dépôt fictif, le build échoue avant même de compiler. Le premier téléchargement est `Downloading from entreprise: https://nexus.mon-entreprise.example/repository/maven-public/org/apache/maven/plugins/maven-resources-plugin/…` : le premier plugin du cycle de vie (module 1). Maven ne peut pas joindre ce serveur, et le dit (`Could not transfer artifact … from/to entreprise`). Le miroir intercepte tout, plugins compris : Maven Central n'est plus jamais appelé.

Avec `repo1.maven.org`, le build passe, et chaque ligne dit `Downloading from entreprise: https://repo1.maven.org/maven2/…` : le nom est celui du miroir, l'URL celle qu'il donne. Les réglages effectifs montrent deux miroirs : `entreprise`, et `maven-default-http-blocker`, que Maven ajoute de lui-même pour refuser tout dépôt en `http://` non chiffré.

En entreprise, c'est la première chose à vérifier quand « Maven ne trouve rien » : quel miroir est appliqué, et vers quelle URL. Si le dépôt exige un mot de passe, une section `<servers>` porte un `<id>` identique à celui du miroir, avec le mot de passe chiffré ou lu dans une variable d'environnement.
</details>

<details>
<summary>5. Chaque réglage à sa place</summary>

1. POM (`maven.compiler.release`) : il fait partie du projet.
2. `settings.xml` : c'est une affaire de poste ; le même projet se construit ailleurs avec un autre dépôt.
3. `settings.xml`, dans `<servers>`, chiffré ou lu dans une variable d'environnement ; jamais en clair, jamais dans un fichier versionné.
4. `.mvn/wrapper/maven-wrapper.properties`.
5. `settings.xml`.
6. Ligne de commande : `-Pci`.
7. POM, ou le BOM qu'il importe (module 3).
</details>

## À retenir

- Le **wrapper** fixe la version de Maven ; `mvnw`, `mvnw.cmd` et `.mvn/wrapper/` se versionnent.
- Le **dépôt local** (`~/.m2/repository`) est le cache de la machine ; `-o` construit sans réseau.
- **`settings.xml` décrit le poste** : miroirs, proxy, identifiants. Un miroir `*` intercepte tous les téléchargements, plugins compris.
- **`help:effective-settings`** montre les réglages réellement appliqués.
- Un mot de passe ne va **jamais en clair**, ni dans un POM, ni dans un fichier versionné.
