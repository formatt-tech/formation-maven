package io.github.formatttech.formation.maven.service;

import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

/** Adresse de la recherche de clients par nom, sur le service hébergé par la machine donnée. */
public final class AdresseDuService {

    private AdresseDuService() {}

    public static URI rechercheParNom(String machine, String nom) throws URISyntaxException {
        HttpHost hote = new HttpHost(machine, 443, "https");
        return new URIBuilder()
                .setScheme(hote.getSchemeName())
                .setHost(hote.getHostName())
                .setPath("/clients")
                .addParameter("nom", nom)
                .build();
    }
}
