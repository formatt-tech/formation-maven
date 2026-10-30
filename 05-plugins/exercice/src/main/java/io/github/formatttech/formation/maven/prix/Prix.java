package io.github.formatttech.formation.maven.prix;

import java.math.BigDecimal;

/** Arrondi d'un prix au centime, la moitié étant arrondie vers le haut (12,345 € donne 12,35 €). */
public final class Prix {

    private Prix() {}

    public static BigDecimal auCentime(BigDecimal prix) {
        return prix.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
