package io.github.formatttech.formation.maven.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

class ImportClientsTest {

    @Test
    void chaqueLigneDonneUnClient() {
        // Given
        String fichier = "nom;ville\nDupont;Lyon\nMartin;Nantes\n";

        // When
        List<String> noms = ImportClients.noms(new StringReader(fichier));

        // Then
        assertEquals(List.of("Dupont", "Martin"), noms);
    }
}
