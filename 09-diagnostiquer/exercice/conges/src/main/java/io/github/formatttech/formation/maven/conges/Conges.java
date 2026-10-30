package io.github.formatttech.formation.maven.conges;

import java.time.LocalDate;
import java.util.List;

/** Les jours de congé posés par un salarié. */
public final class Conges {

    private final List<LocalDate> jours;

    public Conges(List<LocalDate> jours) {
        this.jours = jours.stream().sorted().toList();
    }

    /** Le premier jour de congé à partir d'une date, ou {@code null} s'il n'y en a plus. */
    public LocalDate prochainAPartirDu(LocalDate date) {
        List<LocalDate> aVenir = jours.stream().filter(jour -> !jour.isBefore(date)).toList();
        return aVenir.isEmpty() ? null : aVenir.getFirst();
    }
}
