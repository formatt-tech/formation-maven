package io.github.formatttech.formation.maven.panier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/** Test d'intégration : un panier rempli à partir du catalogue livré avec l'application. */
class CatalogueIT {

    @Test
    void unPanierSeRemplitAuxPrixDuCatalogue() {
        // Given
        Catalogue catalogue = Catalogue.charger();
        Panier panier = new Panier();

        // When
        panier.ajouter(catalogue.ligne("cartable", 1));
        panier.ajouter(catalogue.ligne("stylo", 2));

        // Then
        assertEquals(new BigDecimal("27.90"), panier.total());
    }
}
