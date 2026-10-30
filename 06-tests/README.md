# 6. Les tests : Surefire et Failsafe

**Durée** : 35 minutes.

**À la fin de ce module, vous saurez** :
- quels tests Maven lance, et pourquoi certains ne sont jamais lancés ;
- séparer les tests unitaires des tests d'intégration, et lancer ces derniers au bon moment ;
- ne lancer qu'une partie des tests ;
- sauter les tests en connaissant ce que chaque option saute vraiment.

## Le point de départ

Le projet [`exercice/`](exercice) gère un panier de commande : des lignes, un total, et des codes promotionnels (`PROMO15` : 15 % de remise). Son dossier [`src/test/java`](exercice/src/test/java/io/github/formatttech/formation/maven/panier) contient trois classes de tests :

| Classe | Ce qu'elle teste |
|---|---|
| `PanierTest` | le total du panier |
| `CodePromoTU` | les codes promotionnels (« TU » pour tests unitaires) |
| `CatalogueIT` | un panier rempli à partir du catalogue livré avec l'application (« IT » pour *integration test*) |

```bash
./mvnw -f 06-tests/exercice verify
```

Le build passe. Tous les tests sont donc au vert ? Pas si vite.

## Ce qu'il faut savoir

**Surefire lance les tests unitaires, à la phase `test`.** C'est le plugin attaché d'office à cette phase (module 1). Il ne lance pas toutes les classes de `src/test/java` : seulement celles dont le **nom** suit sa convention, `Test*`, `*Test`, `*Tests` ou `*TestCase`. Une classe qui ne la suit pas est compilée, puis ignorée, sans le moindre message.

**Failsafe lance les tests d'intégration, autour de la phase `verify`.** Un test d'intégration vérifie le projet assemblé, avec ses fichiers et ses voisins (base de données, autre service…) ; il est plus lent, et peut avoir besoin d'un environnement préparé. Failsafe n'est pas dans le cycle de vie : on l'ajoute (module 5). Il lance les classes `IT*`, `*IT` et `*ITCase`, après la phase `package`, sur le jar produit. Il a deux goals :
- `integration-test` lance les tests et **note** les échecs, sans arrêter le build : la phase `post-integration-test` peut ainsi arrêter proprement ce qui a été démarré pour les tests ;
- `verify` relit ces résultats, et fait échouer le build s'il y a eu un échec.

**Chaque plugin écrit un rapport par classe de tests** : `target/surefire-reports` et `target/failsafe-reports`. C'est là qu'on lit le détail d'un échec quand le journal est trop long.

**Les options de la ligne de commande** qu'on rencontre partout :

| Option | Effet |
|---|---|
| `-Dtest=PanierTest` | Surefire ne lance que cette classe (ou ces méthodes : `PanierTest#leTotal*`) |
| `-Dit.test=CatalogueIT` | Failsafe ne lance que cette classe |
| `-DskipTests` | Surefire ne lance pas les tests ; ils sont quand même compilés |
| `-DskipITs` | Failsafe ne lance pas les tests |
| `-Dmaven.test.skip=true` | les tests ne sont **ni compilés ni lancés**, par aucun des deux plugins |

## Travaux pratiques

Les commandes se lancent à la racine du dépôt.

### 1. Quels tests sont lancés ?

```bash
./mvnw -f 06-tests/exercice clean verify
```

Dans le journal, après la ligne `--- surefire … ---`, combien de tests ont tourné, et de quelles classes ? Comparez avec les trois classes du dossier de tests. Regardez aussi le contenu de `06-tests/exercice/target/surefire-reports`.

### 2. Le test qu'on croyait lancé

Renommez la classe `CodePromoTU` en `CodePromoTest` : le fichier `CodePromoTU.java` en `CodePromoTest.java`, puis le nom de la classe dans le fichier. Relancez :

```bash
./mvnw -f 06-tests/exercice verify
```

Que se passe-t-il ? Lisez l'échec dans le journal, puis dans le fichier `target/surefire-reports/…CodePromoTest.txt`. Corrigez le défaut dans [`CodePromo.java`](exercice/src/main/java/io/github/formatttech/formation/maven/panier/CodePromo.java), et relancez jusqu'à ce que le build passe.

### 3. Les tests d'intégration

`CatalogueIT` n'a toujours pas tourné. Ajoutez Failsafe à la fin du POM, avant `</project>` :

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-failsafe-plugin</artifactId>
      <version>3.6.1</version>
      <executions>
        <execution>
          <goals>
            <goal>integration-test</goal>
            <goal>verify</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

Lancez successivement :

```bash
./mvnw -f 06-tests/exercice clean test
./mvnw -f 06-tests/exercice verify
```

Avec `test`, `CatalogueIT` tourne-t-il ? Avec `verify`, entre quelles lignes du journal tourne-t-il ?

Faites maintenant échouer le test d'intégration : dans [`catalogue.csv`](exercice/src/main/resources/catalogue.csv), passez le prix du cartable à `29.90`. Lancez :

```bash
./mvnw -f 06-tests/exercice verify
./mvnw -f 06-tests/exercice integration-test
```

Quel goal arrête le build avec `verify` ? Et que dit la dernière ligne du journal avec `integration-test` ? Remettez le prix à `24.90`.

### 4. Ne lancer qu'une partie des tests

```bash
./mvnw -f 06-tests/exercice verify -Dtest=PanierTest
./mvnw -f 06-tests/exercice test -Dtest='PanierTest#*Vide*'
```

Quels tests tournent dans chaque cas ? Et `CatalogueIT`, dans le premier ?

### 5. Sauter les tests

```bash
./mvnw -f 06-tests/exercice clean verify -DskipTests
```

Quels tests ont tourné ? Le dossier `target/test-classes` existe-t-il ? Puis :

```bash
./mvnw -f 06-tests/exercice clean verify -Dmaven.test.skip=true
```

Mêmes questions. Pour comprendre la différence de comportement de Failsafe, cherchez le paramètre `skipTests` dans sa description :

```bash
./mvnw -f 06-tests/exercice help:describe -Dplugin=failsafe -Dgoal=integration-test -Ddetail
```

## Réponses

<details>
<summary>1. Quels tests sont lancés ?</summary>

Deux tests, tous deux de `PanierTest` : `Tests run: 2`. Le dossier `surefire-reports` ne contient que les rapports de `PanierTest`. `CodePromoTU` ne suit pas la convention de nommage de Surefire ; `CatalogueIT` suit celle de Failsafe, absent du build. Les deux sont compilés, aucun n'est lancé, et rien ne le signale.
</details>

<details>
<summary>2. Le test qu'on croyait lancé</summary>

Le build échoue : `CodePromoTest.laRemiseEstDeduiteDuMontant … expected: <34.00> but was: <6.00>`. Le test était juste, et le défaut était là depuis le début : `appliquer` renvoie la remise au lieu du montant remisé. Correction :

```java
return montant.subtract(remise).setScale(2, RoundingMode.HALF_UP);
```

Un test mal nommé ne protège de rien, et donne une fausse confiance. Dans un projet, on vérifie de temps en temps que le nombre de tests du journal correspond à ce qu'on croit avoir écrit.
</details>

<details>
<summary>3. Les tests d'intégration</summary>

Avec `test`, `CatalogueIT` ne tourne pas : la phase `test` ne va pas jusqu'à Failsafe. Avec `verify`, il tourne après `--- jar … ---`, sous `--- failsafe:3.6.1:integration-test (default) ---`, puis `--- failsafe:3.6.1:verify (default) ---` relit le résultat. Les rapports sont dans `target/failsafe-reports`.

Avec le mauvais prix et `verify`, le test échoue sous `integration-test`, mais c'est le goal `verify` qui arrête le build : `Failed to execute goal …maven-failsafe-plugin:3.6.1:verify`. Avec `integration-test`, le test échoue… et le journal se termine par `BUILD SUCCESS` : la phase `verify` n'a pas été atteinte, personne n'a relu les résultats. On lance donc toujours `verify`, jamais `integration-test`.
</details>

<details>
<summary>4. Ne lancer qu'une partie des tests</summary>

`-Dtest=PanierTest` : Surefire ne lance que les deux tests de `PanierTest`, mais Failsafe lance quand même `CatalogueIT` : `-Dtest` ne concerne que Surefire (pour Failsafe, c'est `-Dit.test`). `-Dtest='PanierTest#*Vide*'` ne lance que `unPanierVideATotalNul` ; les guillemets évitent que le terminal interprète `#` et `*`.
</details>

<details>
<summary>5. Sauter les tests</summary>

Avec `-DskipTests`, Surefire affiche `Tests are skipped.`, mais **`CatalogueIT` tourne** ; `target/test-classes` existe. La description de Failsafe l'explique : son paramètre `skipTests` *n'est pas lié à la propriété `skipTests`* ; il faut `-DskipITs`. C'est le cas des versions récentes de Failsafe (vérifié avec la 3.6.1) : dans un projet plus ancien, `-DskipTests` sautait aussi les tests d'intégration. La description du plugin, pour la version utilisée, fait foi.

Avec `-Dmaven.test.skip=true`, rien ne tourne, et `target/test-classes` n'existe pas : les tests ne sont même pas compilés. Un test qui ne compile plus passe alors inaperçu. À réserver aux cas où l'on n'a vraiment pas besoin des tests, et jamais en CI.
</details>

## À retenir

- Surefire ne lance que les classes `*Test` (et `Test*`, `*Tests`, `*TestCase`) ; Failsafe, les classes `*IT`. **Une classe mal nommée n'est jamais lancée**, sans avertissement.
- Les tests d'intégration tournent après `package`, et c'est le goal `verify` de Failsafe qui fait échouer le build : on lance **`verify`**, jamais `integration-test`.
- `-Dtest` concerne Surefire, `-Dit.test` Failsafe.
- `-DskipTests` saute les tests de Surefire ; `-DskipITs` ceux de Failsafe ; `-Dmaven.test.skip=true` ne les compile même pas. En cas de doute, `help:describe` dit à quelle propriété répond chaque paramètre.
