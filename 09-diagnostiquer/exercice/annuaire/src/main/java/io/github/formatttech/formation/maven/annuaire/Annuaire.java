package io.github.formatttech.formation.maven.annuaire;

import org.apache.commons.lang3.StringUtils;

/** La présentation des noms dans l'annuaire de l'entreprise. */
public final class Annuaire {

    private Annuaire() {}

    /** « jean dupont » devient « Jean DUPONT ». */
    public static String nomAffiche(String prenom, String nom) {
        return StringUtils.capitalize(prenom.toLowerCase()) + " " + nom.toUpperCase();
    }
}
