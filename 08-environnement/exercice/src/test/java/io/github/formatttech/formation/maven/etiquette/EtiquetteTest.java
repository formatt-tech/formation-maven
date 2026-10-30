package io.github.formatttech.formation.maven.etiquette;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EtiquetteTest {

    @Test
    void laVilleEstEnMajusculesSansAccents() {
        // When
        String ligne = Etiquette.ligneVille("38000", "Grenoble-Saint-Égrève");

        // Then
        assertEquals("38000 GRENOBLE-SAINT-EGREVE", ligne);
    }
}
