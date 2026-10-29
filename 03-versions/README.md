# 3. Gérer les versions

**Durée** : 45 minutes.

**À la fin de ce module, vous saurez** :
- reconnaître une famille de bibliothèques dont les versions ne s'accordent pas ;
- déclarer une version à un seul endroit, avec une propriété ;
- aligner toute une famille d'un coup, avec un BOM ;
- prévoir quelle version l'emporte entre ce que dit un BOM et ce que dit une dépendance.

## Le point de départ

Ce projet met des commandes au format JSON avec Jackson, la bibliothèque la plus répandue pour cela. Un collègue vient d'ajouter le module de Jackson qui gère les dates Java, et depuis, le build échoue :

```bash
./mvnw -f 03-versions/exercice verify
```

```text
NoClassDefFound com/fasterxml/jackson/annotation/JsonSerializeAs
```

Le code compile, mais le test s'arrête sur une classe introuvable. Ouvrez [`exercice/pom.xml`](exercice/pom.xml) : les deux dépendances de Jackson n'ont pas la même version.

## Ce qu'il faut savoir

**Une famille de bibliothèques se publie ensemble.** Jackson, comme Spring, JUnit ou Netty, est découpé en plusieurs jars qui sortent toujours avec le même numéro de version, et qui ne fonctionnent qu'entre versions identiques. Mélanger les versions d'une même famille compile souvent, mais casse à l'exécution.

**Une propriété évite de répéter une version.** On la déclare dans `<properties>`, et on l'utilise avec `${…}` :

```xml
<properties>
  <jackson.version>2.22.4</jackson.version>
</properties>
…
<version>${jackson.version}</version>
```

**`<dependencyManagement>` fixe des versions sans ajouter de dépendance.** Ce qu'il contient n'entre pas dans le projet : il dit seulement « si cette bibliothèque arrive, directement ou par transitivité, ce sera dans cette version ». Une dépendance déclarée ensuite dans `<dependencies>` peut alors omettre sa version.

**Un BOM** (*Bill of Materials*, nomenclature) est un POM qui ne contient qu'un `<dependencyManagement>` : la liste des versions qui vont ensemble, publiée par les auteurs de la famille. On l'**importe** dans son propre `<dependencyManagement>`, avec `<type>pom</type>` et `<scope>import</scope>`, et toute la famille est alignée d'un coup.

## Travaux pratiques

Les commandes se lancent à la racine du dépôt.

### 1. Repérer le désalignement

```bash
./mvnw -f 03-versions/exercice dependency:tree -Dverbose -Dincludes=com.fasterxml.jackson.core,com.fasterxml.jackson.datatype
```

L'option `-Dincludes` limite l'arbre aux deux familles de Jackson. Quelle version de `jackson-core` et de `jackson-annotations` est retenue ? Par quel chemin arrive-t-elle ? Et quelle version de `jackson-databind` utilise le projet ? Pourquoi cela casse-t-il ?

### 2. Une seule version, déclarée une fois

Dans le POM, ajoutez dans `<properties>` :

```xml
<jackson.version>2.22.4</jackson.version>
```

Puis remplacez les deux versions de Jackson par `${jackson.version}`, et relancez :

```bash
./mvnw -f 03-versions/exercice verify
./mvnw -f 03-versions/exercice dependency:tree -Dverbose -Dincludes=com.fasterxml.jackson.core,com.fasterxml.jackson.datatype
```

Le build passe-t-il ? Toutes les bibliothèques de Jackson ont-elles la même version ? Qu'est-ce qui garantit l'alignement de `jackson-core`, que le POM ne déclare pas ?

### 3. Aligner toute la famille avec un BOM

Ajoutez, juste avant `<dependencies>` :

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.fasterxml.jackson</groupId>
      <artifactId>jackson-bom</artifactId>
      <version>${jackson.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Puis **retirez** la ligne `<version>` des deux dépendances de Jackson, et relancez les deux commandes de l'exercice 2.

Que signifie la mention `version managed from …` dans l'arbre ? Combien de versions de Jackson le POM déclare-t-il maintenant ?

### 4. Qui l'emporte ?

Remettez une version à une seule des dépendances, celle des dates :

```xml
<artifactId>jackson-datatype-jsr310</artifactId>
<version>2.20.2</version>
```

Relancez l'arbre. Quelle version de `jackson-datatype-jsr310` est retenue : celle du BOM ou celle de la dépendance ? Et pour `jackson-core` ? Le build passe : est-ce rassurant ? Retirez ensuite cette version.

## Réponses

<details>
<summary>1. Repérer le désalignement</summary>

`jackson-core` 2.20.2 et `jackson-annotations` 2.20 arrivent par `jackson-datatype-jsr310` 2.20.2 ; les versions 2.22 qu'apporte `jackson-databind` sont écartées (`omitted for conflict`). Les deux chemins sont aussi longs l'un que l'autre : c'est la dépendance déclarée **en premier** qui l'emporte, ici le module des dates.

Le projet utilise donc `jackson-databind` 2.22.4 avec `jackson-annotations` 2.20 : `databind` 2.22 cherche une annotation apparue après la 2.20, et ne la trouve pas. Changer l'ordre des dépendances dans le POM suffirait à faire passer le build, par hasard : c'est précisément ce qu'il ne faut pas laisser au hasard.
</details>

<details>
<summary>2. Une seule version, déclarée une fois</summary>

Le build passe, et tout Jackson est en 2.22. La propriété n'aligne que les deux dépendances déclarées ; `jackson-core` et `jackson-annotations` suivent parce que, en 2.22.4, les deux modules demandent les mêmes versions. Si un autre chemin apportait `jackson-core` dans une autre version, la propriété n'y pourrait rien.
</details>

<details>
<summary>3. Aligner toute la famille avec un BOM</summary>

`version managed from 2.20.2` : la bibliothèque arrivait dans une version, et `<dependencyManagement>` l'a remplacée par celle du BOM. Le BOM gère aussi les bibliothèques transitives, que le POM ne déclare pas.

Le POM ne déclare plus qu'**une** version de Jackson, celle du BOM, dans la propriété. Monter Jackson, c'est changer une ligne.
</details>

<details>
<summary>4. Qui l'emporte ?</summary>

La version écrite sur la dépendance l'emporte pour elle-même : `jackson-datatype-jsr310` passe en 2.20.2. Mais `jackson-core` et les autres restent en 2.22, gérées par le BOM.

Le build passe, mais le module des dates est maintenant en retard sur le reste de sa famille : c'est le même désalignement qu'au départ, simplement moins visible. La règle : **quand un BOM gère une famille, aucune version sur les dépendances de cette famille**.
</details>

## À retenir

- Une famille de bibliothèques (Jackson, Spring, JUnit…) s'utilise **dans une seule version** ; un mélange compile, puis casse à l'exécution.
- Une version se déclare **une fois** : une propriété, ou mieux, un BOM importé dans `<dependencyManagement>`.
- `<dependencyManagement>` n'ajoute rien au projet : il fixe la version de ce qui arrive, y compris par transitivité.
- Une version écrite sur une dépendance passe avant le BOM : avec un BOM, on n'en écrit pas.
