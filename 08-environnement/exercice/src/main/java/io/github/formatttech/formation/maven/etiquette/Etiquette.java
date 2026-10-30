package io.github.formatttech.formation.maven.etiquette;

import org.apache.commons.lang3.StringUtils;

/** Dernière ligne d'une adresse postale : code postal, puis ville en capitales sans accents. */
public final class Etiquette {

    private Etiquette() {}

    public static String ligneVille(String codePostal, String ville) {
        return codePostal + " " + StringUtils.stripAccents(ville).toUpperCase();
    }
}
