package io.github.formatttech.formation.maven.panier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Les prix des articles, lus dans le fichier catalogue.csv livré avec l'application. */
public final class Catalogue {

    private final Map<String, BigDecimal> prix;

    private Catalogue(Map<String, BigDecimal> prix) {
        this.prix = prix;
    }

    public static Catalogue charger() {
        try (InputStream fichier = Catalogue.class.getResourceAsStream("/catalogue.csv")) {
            if (fichier == null) {
                throw new IllegalStateException("catalogue.csv absent de l'application");
            }
            Map<String, BigDecimal> prix = new HashMap<>();
            new BufferedReader(new InputStreamReader(fichier, StandardCharsets.UTF_8))
                    .lines()
                    .skip(1)
                    .map(ligne -> ligne.split(";"))
                    .forEach(colonnes -> prix.put(colonnes[0], new BigDecimal(colonnes[1])));
            return new Catalogue(prix);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Ligne ligne(String article, int quantite) {
        BigDecimal prixUnitaire = prix.get(article);
        if (prixUnitaire == null) {
            throw new IllegalArgumentException("Article absent du catalogue : " + article);
        }
        return new Ligne(article, prixUnitaire, quantite);
    }
}
