package io.github.formatttech.formation.maven.releve;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Le relevé d'un compte, présenté à la française : 05/10/2026, 1250,00. */
public final class Releve {

    private static final Locale FRANCE = new Locale("fr", "FR");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<Operation> operations;

    public Releve(List<Operation> operations) {
        this.operations = List.copyOf(operations);
    }

    public BigDecimal solde() {
        return operations.stream().map(Operation::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String ligne(Operation operation) {
        return String.format(FRANCE, "%s  %-20s %10.2f",
                operation.date().format(DATE), operation.libelle(), operation.montant());
    }
}
