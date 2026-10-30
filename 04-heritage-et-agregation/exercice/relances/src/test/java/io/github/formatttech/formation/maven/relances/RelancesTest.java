package io.github.formatttech.formation.maven.relances;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.formatttech.formation.maven.facturation.Facture;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class RelancesTest {

    @Test
    void seulesLesFacturesEchuesSontRelancees() {
        // Given
        LocalDate jour = LocalDate.of(2026, 11, 15);
        Facture echue = new Facture("F-001", LocalDate.of(2026, 10, 31), false);
        Facture payee = new Facture("F-002", LocalDate.of(2026, 10, 31), true);
        Facture aVenir = new Facture("F-003", LocalDate.of(2026, 11, 30), false);

        // When
        List<Facture> aRelancer = Relances.aRelancerLe(jour, List.of(echue, payee, aVenir));

        // Then
        assertEquals(List.of(echue), aRelancer);
    }
}
