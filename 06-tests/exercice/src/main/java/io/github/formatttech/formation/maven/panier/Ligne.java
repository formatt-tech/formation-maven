package io.github.formatttech.formation.maven.panier;

import java.math.BigDecimal;

/** Une ligne du panier : un article, son prix unitaire et la quantité commandée. */
public record Ligne(String article, BigDecimal prixUnitaire, int quantite) {

    public BigDecimal montant() {
        return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }
}
