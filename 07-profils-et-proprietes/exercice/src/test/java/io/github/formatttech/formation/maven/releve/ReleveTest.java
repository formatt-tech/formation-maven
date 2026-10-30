package io.github.formatttech.formation.maven.releve;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

class ReleveTest {

    private static final Operation SALAIRE =
            new Operation(LocalDate.of(2026, 10, 1), "Salaire", new BigDecimal("2000.00"));
    private static final Operation LOYER =
            new Operation(LocalDate.of(2026, 10, 5), "Loyer", new BigDecimal("-750.00"));

    @Test
    void leSoldeEstLaSommeDesOperations() {
        // Given
        Releve releve = new Releve(List.of(SALAIRE, LOYER));

        // When
        BigDecimal solde = releve.solde();

        // Then
        assertEquals(new BigDecimal("1250.00"), solde);
    }

    @Test
    void uneLigneSePresenteALaFrancaise() {
        // Given
        Releve releve = new Releve(List.of(LOYER));

        // When
        String ligne = releve.ligne(LOYER);

        // Then
        assertEquals("05/10/2026  Loyer                   -750,00", ligne);
    }
}
