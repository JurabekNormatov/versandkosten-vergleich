package com.jurabek.versand.service;

import com.jurabek.versand.model.VersandAnfrage;
import com.jurabek.versand.model.VersandAngebot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * DPDService — zuständig NUR für DPD-Preisabfragen.
 * DPD ist oft günstiger bei schweren Paketen.
 */
@Slf4j
@Service
public class DPDService {

    private static final double DPD_GRUNDPREIS = 4.20;
    private static final double DPD_PREIS_PRO_KG = 0.40;
    private static final double DPD_AUSLAND_AUFSCHLAG = 4.50;
    private static final double DPD_VOLUMEN_DIVISOR = 5000.0;

    public VersandAngebot berechnePreis(VersandAnfrage anfrage) {
        log.info("DPD Preisberechnung gestartet für {}kg", anfrage.getGewichtKg());

        try {
            double volumengewicht = (anfrage.getLaengeCm()
                    * anfrage.getBreiteCm()
                    * anfrage.getHoeheCm()) / DPD_VOLUMEN_DIVISOR;

            double abrechnungsgewicht = Math.max(
                    anfrage.getGewichtKg(),
                    volumengewicht
            );

            double preis = DPD_GRUNDPREIS + (abrechnungsgewicht * DPD_PREIS_PRO_KG);

            boolean istAusland = !anfrage.getZielland().equalsIgnoreCase("DE");
            if (istAusland) {
                preis += DPD_AUSLAND_AUFSCHLAG;
            }

            preis = Math.round(preis * 100.0) / 100.0;

            log.info("DPD Preis berechnet: {}€", preis);

            return VersandAngebot.builder()
                    .anbieter("DPD")
                    .preisEuro(preis)
                    .lieferdauerWerktage(istAusland ? 4 : 2)
                    .guenstigster(false)
                    .build();

        } catch (Exception e) {
            log.error("DPD Preisberechnung fehlgeschlagen: {}", e.getMessage());
            return VersandAngebot.builder()
                    .anbieter("DPD")
                    .fehler("DPD Preisberechnung momentan nicht verfügbar")
                    .build();
        }
    }
}