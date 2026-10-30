package io.github.formatttech.formation.maven.panier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class PanierTest {

    @Test
    void unPanierVideATotalNul() {
        // Given
        Panier panier = new Panier();

        // When
        BigDecimal total = panier.total();

        // Then
        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void leTotalEstLaSommeDesLignes() {
        // Given
        Panier panier = new Panier();
        panier.ajouter(new Ligne("stylo", new BigDecimal("1.50"), 4));
        panier.ajouter(new Ligne("cahier", new BigDecimal("3.20"), 2));

        // When
        BigDecimal total = panier.total();

        // Then
        assertEquals(new BigDecimal("12.40"), total);
    }
}
