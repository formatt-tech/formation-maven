package io.github.formatttech.formation.maven.facturation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class FactureTest {

    private static final LocalDate ECHEANCE = LocalDate.of(2026, 10, 31);

    @Test
    void uneFactureNonPayeeNEstPasEchueLeJourDeSonEcheance() {
        // Given
        Facture facture = new Facture("F-001", ECHEANCE, false);

        // When
        boolean echue = facture.estEchueLe(ECHEANCE);

        // Then
        assertFalse(echue);
    }

    @Test
    void uneFactureNonPayeeEstEchueAuLendemainDeSonEcheance() {
        // Given
        Facture facture = new Facture("F-001", ECHEANCE, false);

        // When
        boolean echue = facture.estEchueLe(ECHEANCE.plusDays(1));

        // Then
        assertTrue(echue);
    }

    @Test
    void uneFacturePayeeNEstJamaisEchue() {
        // Given
        Facture facture = new Facture("F-002", ECHEANCE, true);

        // When
        boolean echue = facture.estEchueLe(ECHEANCE.plusDays(30));

        // Then
        assertFalse(echue);
    }
}
