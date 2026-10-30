package io.github.formatttech.formation.maven.facturation;

import java.time.LocalDate;

/** Une facture émise, et la date à laquelle elle doit être payée. */
public record Facture(String numero, LocalDate echeance, boolean payee) {

    /** Une facture est échue quand elle n'est pas payée au lendemain de son échéance. */
    public boolean estEchueLe(LocalDate jour) {
        return !payee && jour.isAfter(echeance);
    }
}
