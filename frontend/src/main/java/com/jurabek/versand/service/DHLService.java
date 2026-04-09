package com.jurabek.versand.service;

import com.jurabek.versand.model.VersandAnfrage;
import com.jurabek.versand.model.VersandAngebot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * DHLService — zuständig NUR für DHL-Preisabfragen.
 *
 * @Service — markiert diese Klasse als Spring Service-Bean.
 * Spring erstellt automatisch eine Instanz davon und stellt
 * sie per Dependency Injection bereit (wie wir es im Controller gesehen haben).
 *
 * HINWEIS: Da DHL keine kostenlose öffentliche API hat,
 * simulieren wir die Preisberechnung mit echter Logik.
 * Die Formel basiert auf echten DHL-Tarifen (Stand 2024).
 * In Phase 3 zeige ich dir wie man echte APIs anbindet.
 */
@Slf4j
@Service
public class DHLService {

    /*
     * Preistabelle DHL Paket (Deutschland, vereinfacht)
     * Quelle: DHL Geschäftskundenpreise 2024
     *
     * In echten Projekten kämen diese Werte aus einer Datenbank
     * oder einer Konfigurationsdatei (application.properties).
     * Hardcodierte Werte im Code nennt man "Magic Numbers" —
     * das vermeidet man in der Praxis. Für unser Projekt ist es okay.
     */
    private static final double DHL_GRUNDPREIS = 3.99;
    private static final double DHL_PREIS_PRO_KG = 0.50;
    private static final double DHL_AUSLAND_AUFSCHLAG = 5.00;
    private static final double DHL_VOLUMEN_DIVISOR = 5000.0;

    /**
     * Berechnet den DHL-Versandpreis für eine Anfrage.
     *
     * @param anfrage — die Nutzereingabe mit Gewicht, Maßen und Ländern
     * @return VersandAngebot mit DHL-Preis, oder Fehler falls etwas schiefgeht
     */
    public VersandAngebot berechnePreis(VersandAnfrage anfrage) {
        log.info("DHL Preisberechnung gestartet für {}kg", anfrage.getGewichtKg());

        try {
            // Volumenwicht berechnen
            // Formel: (Länge × Breite × Höhe) / 5000
            // Viele Anbieter nehmen das Maximum aus echtem Gewicht
            // und Volumenwicht — das schützt sie vor großen leichten Paketen.
            double volumengewicht = (anfrage.getLaengeCm()
                    * anfrage.getBreiteCm()
                    * anfrage.getHoeheCm()) / DHL_VOLUMEN_DIVISOR;

            // Abrechnungsgewicht = das höhere von beiden
            double abrechnungsgewicht = Math.max(
                    anfrage.getGewichtKg(),
                    volumengewicht
            );

            // Basispreis berechnen
            double preis = DHL_GRUNDPREIS + (abrechnungsgewicht * DHL_PREIS_PRO_KG);

            // Auslandsversand — anderes Land als Deutschland?
            boolean istAusland = !anfrage.getZielland().equalsIgnoreCase("DE");
            if (istAusland) {
                preis += DHL_AUSLAND_AUFSCHLAG;
                log.info("DHL Auslandsaufschlag angewendet: +{}€", DHL_AUSLAND_AUFSCHLAG);
            }

            // Preis auf 2 Dezimalstellen runden
            preis = Math.round(preis * 100.0) / 100.0;

            log.info("DHL Preis berechnet: {}€", preis);

            // Angebot zurückgeben mit Builder-Pattern
            return VersandAngebot.builder()
                    .anbieter("DHL")
                    .preisEuro(preis)
                    .lieferdauerWerktage(istAusland ? 5 : 2)
                    .guenstigster(false) // wird später im VersandService gesetzt
                    .build();

        } catch (Exception e) {
            // Fehlerfall — wir geben kein null zurück sondern ein
            // Angebot-Objekt mit Fehlermeldung. Warum?
            // Weil null im Controller zu NullPointerException führt.
            // So kann das Frontend den Fehler sauber anzeigen.
            log.error("DHL Preisberechnung fehlgeschlagen: {}", e.getMessage());
            return VersandAngebot.builder()
                    .anbieter("DHL")
                    .fehler("DHL Preisberechnung momentan nicht verfügbar")
                    .build();
        }
    }
}