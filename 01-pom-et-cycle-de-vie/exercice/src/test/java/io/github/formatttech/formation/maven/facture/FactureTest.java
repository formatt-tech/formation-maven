package io.github.formatttech.formation.maven.facture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class FactureTest {

    @Test
    void leMontantTtcAjouteLaTvaAuMontantHt() {
        // Given
        Facture facture = new Facture(new BigDecimal("100.00"), new BigDecimal("0.20"));

        // When
        BigDecimal ttc = facture.montantTtc();

        // Then
        assertEquals(new BigDecimal("120.00"), ttc);
    }

    @Test
    void leMontantTtcEstArrondiAuCentime() {
        // Given
        Facture facture = new Facture(new BigDecimal("9.99"), new BigDecimal("0.055"));

        // When
        BigDecimal ttc = facture.montantTtc();

        // Then
        assertEquals(new BigDecimal("10.54"), ttc);
    }
}
