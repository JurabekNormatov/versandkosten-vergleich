package com.jurabek.versand.service;

import com.jurabek.versand.model.VersandAnfrage;
import com.jurabek.versand.model.VersandAngebot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * HermesService — zuständig NUR für Hermes-Preisabfragen.
 * Hermes ist bekannt für günstigere Preise bei leichten Paketen.
 */
@Slf4j
@Service
public class HermesService {

    private static final double HERMES_GRUNDPREIS = 3.50;
    private static final double HERMES_PREIS_PRO_KG = 0.45;
    private static final double HERMES_AUSLAND_AUFSCHLAG = 6.50;
    private static final double HERMES_VOLUMEN_DIVISOR = 5000.0;

    // Hermes hat ein Maximalgewicht von 25kg
    private static final double HERMES_MAX_GEWICHT = 25.0;

    public VersandAngebot berechnePreis(VersandAnfrage anfrage) {
        log.info("Hermes Preisberechnung gestartet für {}kg", anfrage.getGewichtKg());

        try {
            // Gewichtslimit prüfen — Hermes nimmt keine schweren Pakete
            if (anfrage.getGewichtKg() > HERMES_MAX_GEWICHT) {
                log.warn("Hermes: Paket zu schwer ({}kg > {}kg)",
                        anfrage.getGewichtKg(), HERMES_MAX_GEWICHT);
                return VersandAngebot.builder()
                        .anbieter("Hermes")
                        .fehler("Paket überschreitet Hermes Maximalgewicht von 25kg")
                        .build();
            }

            double volumengewicht = (anfrage.getLaengeCm()
                    * anfrage.getBreiteCm()
                    * anfrage.getHoeheCm()) / HERMES_VOLUMEN_DIVISOR;

            double abrechnungsgewicht = Math.max(
                    anfrage.getGewichtKg(),
                    volumengewicht
            );

            double preis = HERMES_GRUNDPREIS + (abrechnungsgewicht * HERMES_PREIS_PRO_KG);

            boolean istAusland = !anfrage.getZielland().equalsIgnoreCase("DE");
            if (istAusland) {
                preis += HERMES_AUSLAND_AUFSCHLAG;
            }

            preis = Math.round(preis * 100.0) / 100.0;

            log.info("Hermes Preis berechnet: {}€", preis);

            return VersandAngebot.builder()
                    .anbieter("Hermes")
                    .preisEuro(preis)
                    .lieferdauerWerktage(istAusland ? 7 : 3)
                    .guenstigster(false)
                    .build();

        } catch (Exception e) {
            log.error("Hermes Preisberechnung fehlgeschlagen: {}", e.getMessage());
            return VersandAngebot.builder()
                    .anbieter("Hermes")
                    .fehler("Hermes Preisberechnung momentan nicht verfügbar")
                    .build();
        }
    }
}