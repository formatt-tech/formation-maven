package io.github.formatttech.formation.maven.commande;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Une commande, telle qu'elle est échangée en JSON avec les autres applications. */
public record Commande(String reference, LocalDate date, BigDecimal montant) {}
