# Formation Maven

[![Licence Apache 2.0](https://img.shields.io/badge/licence-Apache%202.0-blue.svg)](LICENSE)
![Java 25](https://img.shields.io/badge/Java-25-orange.svg)

L'essentiel de Maven pour travailler sur un projet professionnel, par la pratique : chaque module explique une notion
en une page, puis la fait manipuler sur un petit projet, avec les réponses à la fin.

Pour qui : développeurs Java qui utilisent Maven sans l'avoir appris, et veulent comprendre ce que fait le build,
d'où vient une dépendance, ou pourquoi un build échoue.

## Démarrer

[![Ouvrir dans GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/formatt-tech/formation-maven)

Un environnement prêt s'ouvre dans le navigateur : Java 25, Maven, et les dépendances des exercices déjà
téléchargées. Il faut un compte GitHub ; Codespaces offre un quota mensuel gratuit aux comptes personnels.

Sur son poste : Java 25 suffit, le dépôt fournit Maven (`./mvnw`, ou `.\mvnw.cmd` sous Windows). Avec Docker, le
dépôt s'ouvre aussi en Dev Container dans VS Code ou IntelliJ.

## Programme

L'ensemble tient en une demi-journée ; chaque module peut aussi se suivre seul.

| # | Module | Durée |
|---|---|---|
| 1 | [Le POM et le cycle de vie](01-pom-et-cycle-de-vie/README.md) | 30 min |
| 2 | [Les dépendances](02-dependances/README.md) | 30 min |
| 3 | [Gérer les versions](03-versions/README.md) | 25 min |
| 4 | [Héritage et agrégation](04-heritage-et-agregation/README.md) | 30 min |
| 5 | [Les plugins](05-plugins/README.md) | 30 min |
| 6 | [Les tests : Surefire et Failsafe](06-tests/README.md) | 35 min |
| 7 | [Profils et propriétés](07-profils-et-proprietes/README.md) | 35 min |
| 8 | [L'environnement : wrapper, dépôt local, `settings.xml`](08-environnement/README.md) | 30 min |
| 9 | [Diagnostiquer un build](09-diagnostiquer/README.md) | 30 min |

## Licence

[Apache 2.0](LICENSE) — Matthieu Ladrat ([formatt](https://github.com/formatt-tech)).
