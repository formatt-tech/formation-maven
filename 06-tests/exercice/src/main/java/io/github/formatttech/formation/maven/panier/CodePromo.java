package io.github.formatttech.formation.maven.panier;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Un code promotionnel de la forme PROMO15 : 15 % de remise sur le total. */
public record CodePromo(int pourcentage) {

    private static final String PREFIXE = "PROMO";

    public static CodePromo lire(String code) {
        if (!code.startsWith(PREFIXE)) {
            throw new IllegalArgumentException("Code promotionnel inconnu : " + code);
        }
        return new CodePromo(Integer.parseInt(code.substring(PREFIXE.length())));
    }

    /** Le montant à payer une fois la remise déduite, au centime. */
    public BigDecimal appliquer(BigDecimal montant) {
        BigDecimal remise = montant.multiply(BigDecimal.valueOf(pourcentage))
                .divide(BigDecimal.valueOf(100));
        return remise.setScale(2, RoundingMode.HALF_UP);
    }
}
