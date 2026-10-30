package io.github.formatttech.formation.maven.releve;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Une opération du compte : un débit a un montant négatif. */
public record Operation(LocalDate date, String libelle, BigDecimal montant) {}
