package com.jurabek.versand.service;

import com.jurabek.versand.model.VersandAnfrage;
import com.jurabek.versand.model.VersandAngebot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * VersandService — der Koordinator.
 *
 * Er ruft alle drei Anbieter-Services auf,
 * sammelt die Ergebnisse und markiert das günstigste Angebot.
 *
 * Dieser Service kennt keine Details über DHL, Hermes oder DPD —
 * er delegiert die Arbeit und koordiniert nur das Ergebnis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VersandService {

    // Alle drei Services werden automatisch injiziert
    private final DHLService dhlService;
    private final HermesService hermesService;
    private final DPDService dpdService;

    /**
     * Hauptmethode — ruft alle Anbieter auf und vergleicht die Preise.
     *
     * @param anfrage — die Nutzereingabe
     * @return Liste aller Angebote, günstigstes ist markiert
     */
    public List<VersandAngebot> vergleichen(VersandAnfrage anfrage) {
        log.info("Starte Preisvergleich für alle Anbieter");

        // Liste für alle Angebote
        List<VersandAngebot> angebote = new ArrayList<>();

        // Alle drei Anbieter aufrufen
        // Wichtig: Wir fangen keine Exceptions hier — das machen
        // die einzelnen Services bereits intern. So kommt immer
        // ein Angebot zurück, entweder mit Preis oder mit Fehler.
        angebote.add(dhlService.berechnePreis(anfrage));
        angebote.add(hermesService.berechnePreis(anfrage));
        angebote.add(dpdService.berechnePreis(anfrage));

        // Günstigstes Angebot finden und markieren
        // Nur Angebote ohne Fehler werden verglichen
        angebote.stream()
                .filter(a -> a.getFehler() == null)  // nur gültige Angebote
                .min(Comparator.comparingDouble(VersandAngebot::getPreisEuro))
                .ifPresent(guenstigstes -> {
                    guenstigstes.setGuenstigster(true);
                    log.info("Günstigstes Angebot: {} mit {}€",
                            guenstigstes.getAnbieter(),
                            guenstigstes.getPreisEuro());
                });

        return angebote;
    }
}