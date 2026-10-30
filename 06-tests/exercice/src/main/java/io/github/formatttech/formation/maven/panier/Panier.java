package io.github.formatttech.formation.maven.panier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Les lignes d'une commande en cours, et leur total. */
public final class Panier {

    private final List<Ligne> lignes = new ArrayList<>();

    public void ajouter(Ligne ligne) {
        lignes.add(ligne);
    }

    public BigDecimal total() {
        return lignes.stream().map(Ligne::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalAvec(CodePromo codePromo) {
        return codePromo.appliquer(total());
    }
}
