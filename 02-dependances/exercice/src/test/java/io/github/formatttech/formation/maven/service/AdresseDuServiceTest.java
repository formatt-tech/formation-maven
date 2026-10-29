package io.github.formatttech.formation.maven.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.net.URI;

class AdresseDuServiceTest {

    @Test
    void leNomRechercheEstEncodeDansLAdresse() throws Exception {
        // When
        URI adresse = AdresseDuService.rechercheParNom("api.exemple.fr", "Dupont & Fils");

        // Then
        assertEquals("https://api.exemple.fr/clients?nom=Dupont+%26+Fils", adresse.toString());
    }
}
