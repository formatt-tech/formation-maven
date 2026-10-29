package io.github.formatttech.formation.maven.facture;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Montant d'une facture : hors taxes, et toutes taxes comprises au taux de TVA donné. */
public record Facture(BigDecimal montantHt, BigDecimal tauxDeTva) {

    public BigDecimal montantTtc() {
        return montantHt
                .multiply(BigDecimal.ONE.add(tauxDeTva))
                .setScale(2, RoundingMode.HALF_EVEN);
    }
}
