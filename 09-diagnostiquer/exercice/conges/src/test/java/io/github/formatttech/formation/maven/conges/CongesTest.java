package io.github.formatttech.formation.maven.conges;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class CongesTest {

    @Test
    void leProchainCongeEstLePlusProcheAVenir() {
        // Given
        Conges conges = new Conges(List.of(
                LocalDate.of(2026, 12, 24), LocalDate.of(2026, 8, 14), LocalDate.of(2026, 11, 2)));

        // When
        LocalDate prochain = conges.prochainAPartirDu(LocalDate.of(2026, 10, 30));

        // Then
        assertEquals(LocalDate.of(2026, 11, 2), prochain);
    }
}
