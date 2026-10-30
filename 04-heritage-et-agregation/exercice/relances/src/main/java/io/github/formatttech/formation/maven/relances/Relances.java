package io.github.formatttech.formation.maven.relances;

import io.github.formatttech.formation.maven.facturation.Facture;

import java.time.LocalDate;
import java.util.List;

/** Choisit, parmi les factures, celles qu'il faut relancer ce jour-là. */
public final class Relances {

    private Relances() {}

    public static List<Facture> aRelancerLe(LocalDate jour, List<Facture> factures) {
        return factures.stream().filter(facture -> facture.estEchueLe(jour)).toList();
    }
}
