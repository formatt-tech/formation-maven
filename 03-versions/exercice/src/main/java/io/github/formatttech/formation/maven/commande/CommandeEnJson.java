package io.github.formatttech.formation.maven.commande;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** Met une commande au format JSON, avec ses dates au format ISO (2026-10-29). */
public final class CommandeEnJson {

    private static final ObjectMapper JSON =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private CommandeEnJson() {}

    public static String de(Commande commande) throws JsonProcessingException {
        return JSON.writeValueAsString(commande);
    }
}
