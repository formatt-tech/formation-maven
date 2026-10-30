package io.github.formatttech.formation.maven.annuaire;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AnnuaireTest {

    @Test
    void lePrenomPrendUneMajusculeEtLeNomEstEnCapitales() {
        // When
        String nomAffiche = Annuaire.nomAffiche("jean", "dupont");

        // Then
        assertEquals("Jean DUPONT", nomAffiche);
    }
}
