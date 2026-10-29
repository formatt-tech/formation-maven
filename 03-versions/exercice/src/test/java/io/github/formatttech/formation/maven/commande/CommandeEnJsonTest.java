package io.github.formatttech.formation.maven.commande;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

class CommandeEnJsonTest {

    @Test
    void laDateDeLaCommandeEstAuFormatIso() throws Exception {
        // Given
        Commande commande =
                new Commande("C-2026-001", LocalDate.of(2026, 10, 29), new BigDecimal("42.50"));

        // When
        String json = CommandeEnJson.de(commande);

        // Then
        assertEquals(
                "{\"reference\":\"C-2026-001\",\"date\":\"2026-10-29\",\"montant\":42.50}", json);
    }
}
