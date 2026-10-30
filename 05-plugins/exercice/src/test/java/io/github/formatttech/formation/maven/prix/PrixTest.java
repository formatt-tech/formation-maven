package io.github.formatttech.formation.maven.prix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class PrixTest {

    @Test
    void unDemiCentimeEstArrondiAuCentimeSuperieur() {
        // When
        BigDecimal arrondi = Prix.auCentime(new BigDecimal("12.345"));

        // Then
        assertEquals(new BigDecimal("12.35"), arrondi);
    }
}
