#!/usr/bin/env bash
# Télécharge à l'avance tout ce dont les exercices ont besoin : Maven, plugins, dépendances.
# Les exercices peuvent échouer exprès : un échec ici n'arrête pas la préparation.
set -u
for pom in $(find . -path '*/exercice/*' -name pom.xml -not -path '*/target/*' | sort); do
  projet=$(dirname "$pom")
  echo "Préparation de $projet"
  ./mvnw -B -ntp -q -f "$projet" verify dependency:tree dependency:analyze install:help \
    help:effective-pom -Doutput=target/pom-effectif.xml > /dev/null 2>&1 || true
  ./mvnw -B -ntp -q -f "$projet" clean > /dev/null 2>&1 || true
done
echo "Environnement prêt."
