package io.github.formatttech.formation.maven.panier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/** Tests unitaires du code promotionnel. */
class CodePromoTU {

    @Test
    void lePourcentageSuitLePrefixe() {
        // When
        CodePromo codePromo = CodePromo.lire("PROMO15");

        // Then
        assertEquals(15, codePromo.pourcentage());
    }

    @Test
    void laRemiseEstDeduiteDuMontant() {
        // Given
        CodePromo codePromo = new CodePromo(15);

        // When
        BigDecimal aPayer = codePromo.appliquer(new BigDecimal("40.00"));

        // Then
        assertEquals(new BigDecimal("34.00"), aPayer);
    }
}
