# 5. Les plugins

**Durée** : 30 minutes.

**À la fin de ce module, vous saurez** :
- d'où viennent les versions des plugins qu'on ne déclare pas ;
- configurer un plugin du cycle de vie, et quand écrire sa version ;
- ajouter au build un plugin qui n'y est pas d'office, et choisir la phase où il s'exécute ;
- distinguer `<plugins>` de `<pluginManagement>`.

## Le point de départ

Ouvrez [`exercice/pom.xml`](exercice/pom.xml) : comme au module 1, aucun plugin n'est déclaré. Et pourtant le build compile, teste et assemble un jar, avec des plugins dont le journal affiche la version. Lancez :

```bash
./mvnw -f 05-plugins/exercice verify
```

Le build passe, mais le journal contient une note du compilateur sur [`Prix.java`](exercice/src/main/java/io/github/formatttech/formation/maven/prix/Prix.java) : `uses or overrides a deprecated API`, puis `Recompile with -Xlint:deprecation for details`. Pour voir le détail, il faut changer la façon dont Maven appelle le compilateur. Comment, puisque le POM ne parle pas du compilateur ?

## Ce qu'il faut savoir

**Les versions des plugins du cycle de vie viennent de Maven.** Chaque version de Maven attache aux phases (module 1) une version précise de chaque plugin : `compiler`, `surefire`, `jar`… Changer de version de Maven, c'est changer de version de plugins. Le wrapper du projet (module 8) fixe la version de Maven, donc celles des plugins.

**On configure un plugin dans `<build><plugins>`.** On y déclare le plugin par ses coordonnées, et on ajoute une `<configuration>`. Dès qu'on configure un plugin, on écrit aussi sa **version** : sa configuration est écrite pour une version donnée, et Maven avertit si elle manque.

**Un plugin qui n'est pas dans le cycle de vie doit être ajouté.** Le contrôle des règles du build (Enforcer), la couverture des tests (JaCoCo)… ne s'exécutent que si on les déclare, avec une **exécution** (`<execution>`) qui dit quel goal lancer, et à quelle phase s'il ne le sait pas déjà.

**`<pluginManagement>` configure sans activer.** Comme `<dependencyManagement>` pour les dépendances (module 3), il fixe la version et la configuration d'un plugin **au cas où** un projet l'utilise. On s'en sert surtout dans un POM parent : il prépare les plugins, chaque projet choisit ceux qu'il active dans `<plugins>`.

## Travaux pratiques

Les commandes se lancent à la racine du dépôt.

### 1. Les versions viennent de Maven

Relancez le build, et relevez les versions de `compiler`, `surefire` et `jar` dans les lignes `--- … ---` du journal :

```bash
./mvnw -f 05-plugins/exercice clean verify
```

Le wrapper du dépôt utilise Maven 3.10.0. Faites-lui utiliser Maven 3.9.11, puis relancez le build :

```bash
./mvnw -N wrapper:wrapper -Dmaven=3.9.11
./mvnw -f 05-plugins/exercice clean verify
```

Quelles versions de plugins voyez-vous maintenant ? Le POM de l'exercice a-t-il changé ? Remettez ensuite le wrapper d'origine :

```bash
git checkout -- .mvn
```

### 2. Configurer un plugin du cycle de vie

Ajoutez à la fin du POM, avant `</project>` :

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <compilerArgs>
          <arg>-Xlint:all</arg>
        </compilerArgs>
      </configuration>
    </plugin>
  </plugins>
</build>
```

`-Xlint:all` demande au compilateur Java d'afficher tous ses avertissements.

```bash
./mvnw -f 05-plugins/exercice clean verify
```

Quels avertissements le compilateur affiche-t-il sur `Prix.java` ? Et quel avertissement Maven affiche-t-il, tout en haut du journal ? Corrigez-le, puis relancez.

### 3. Ajouter un plugin qui n'est pas dans le cycle de vie

Remplacez la section `<build>` de l'exercice 2 par celle-ci. Elle prépare Maven Enforcer, un plugin qui vérifie des règles sur l'environnement du build ; la règle exige ici un Java de version 99 au moins, pour la voir échouer :

```xml
<build>
  <pluginManagement>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-enforcer-plugin</artifactId>
        <version>3.6.3</version>
        <executions>
          <execution>
            <id>verifier-java</id>
            <goals>
              <goal>enforce</goal>
            </goals>
            <configuration>
              <rules>
                <requireJavaVersion>
                  <version>[99,)</version>
                </requireJavaVersion>
              </rules>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </pluginManagement>
</build>
```

```bash
./mvnw -f 05-plugins/exercice verify
```

Le build passe-t-il ? Le journal mentionne-t-il `enforcer` ? Ajoutez maintenant, dans `<build>`, après `</pluginManagement>` :

```xml
<plugins>
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
  </plugin>
</plugins>
```

Relancez. Le build passe-t-il ? Relevez la ligne `--- enforcer … ---` : quel nom d'exécution y voyez-vous ? À quelle phase s'est-il exécuté, et qu'est-ce que ça change pour la suite du build ?

## Réponses

<details>
<summary>1. Les versions viennent de Maven</summary>

Avec Maven 3.10.0 : `compiler:3.16.0`, `surefire:3.6.0`, `jar:3.5.1`. Avec Maven 3.9.11 : `compiler:3.13.0`, `surefire:3.2.5`, `jar:3.4.1`. Le POM n'a pas changé : seule la version de Maven, inscrite dans `.mvn/wrapper/maven-wrapper.properties`, a changé. D'où l'intérêt du wrapper : toute l'équipe et la CI construisent avec les mêmes plugins.
</details>

<details>
<summary>2. Configurer un plugin du cycle de vie</summary>

Le compilateur détaille deux avertissements : `ROUND_HALF_UP in java.math.BigDecimal has been deprecated`, et `setScale(int,int) … has been deprecated`. La correction serait `setScale(2, RoundingMode.HALF_UP)`.

Maven avertit : `'build.plugins.plugin.version' for org.apache.maven.plugins:maven-compiler-plugin is missing`, et prévient qu'une future version de Maven pourrait refuser ce POM. On ajoute `<version>3.16.0</version>`, la version que Maven 3.10.0 utilisait déjà : l'avertissement disparaît, et la configuration reste attachée à la version pour laquelle elle a été écrite.
</details>

<details>
<summary>3. Ajouter un plugin qui n'est pas dans le cycle de vie</summary>

Avec `<pluginManagement>` seul, le build passe, et `enforcer` n'apparaît nulle part : le plugin est préparé, pas activé.

Une fois déclaré dans `<plugins>`, il s'exécute : `--- enforcer:3.6.3:enforce (verifier-java) @ prix ---`. `verifier-java` est le nom donné à l'exécution ; la version et la configuration viennent de `<pluginManagement>`, le projet n'a eu qu'à l'activer. Le goal `enforce` s'attache de lui-même à la phase `validate`, la toute première : le build s'arrête sur la règle (`RequireJavaVersion failed`) avant même de compiler.
</details>

## À retenir

- Les plugins du cycle de vie ont la version que leur donne **Maven** ; le wrapper fixe Maven, donc les plugins.
- On **configure** un plugin dans `<plugins>`, et dès qu'on le configure, on écrit sa **version**.
- Un plugin hors du cycle de vie s'ajoute avec une **exécution** ; son nom apparaît dans le journal.
- `<pluginManagement>` prépare sans activer ; `<plugins>` active. Un parent prépare, chaque projet active.
