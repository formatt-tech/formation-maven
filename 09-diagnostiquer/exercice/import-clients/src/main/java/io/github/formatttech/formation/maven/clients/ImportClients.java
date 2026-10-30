package io.github.formatttech.formation.maven.clients;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.List;

/** Les noms des clients d'un fichier CSV : points-virgules, ligne d'en-tête. */
public final class ImportClients {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')
            .setHeader()
            .setSkipHeaderRecord(true)
            .get();

    private ImportClients() {}

    public static List<String> noms(Reader fichier) {
        try {
            return FORMAT.parse(fichier).stream().map(ImportClients::nom).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String nom(CSVRecord ligne) {
        return ligne.get("nom");
    }
}
