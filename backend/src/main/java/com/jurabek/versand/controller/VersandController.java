package com.jurabek.versand.controller;

import com.jurabek.versand.model.VersandAnfrage;
import com.jurabek.versand.model.VersandAngebot;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VersandController — das Eingangstor der REST API.
 *
 * @RestController  — Kombination aus @Controller + @ResponseBody.
 *                   Bedeutet: diese Klasse behandelt HTTP-Requests
 *                   und gibt automatisch JSON zurück (kein HTML).
 *
 * @RequestMapping  — Alle Endpoints in dieser Klasse beginnen mit /api/v1/versand
 *                   "v1" ist Versionierung — professioneller Standard.
 *                   Wenn du später breaking changes machst, baust du /api/v2/
 *                   ohne die alten Nutzer zu stören.
 *
 * @RequiredArgsConstructor — Lombok generiert einen Konstruktor für alle
 *                            "final" Felder. So wird der VersandService
 *                            automatisch injiziert (Dependency Injection).
 *
 * @Slf4j — Lombok erstellt automatisch einen Logger.
 *          Statt System.out.println() nutzen wir log.info() —
 *          das ist der professionelle Standard.
 *          In echten Projekten landen diese Logs in Monitoring-Tools
 *          wie Grafana oder Splunk.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/versand")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
/*
 * @CrossOrigin — Erlaubt Anfragen von anderen Domains (z.B. dein UI5-Frontend).
 * Ohne das blockiert der Browser alle Anfragen vom Frontend zum Backend.
 * Das nennt sich CORS (Cross-Origin Resource Sharing).
 * In Produktion würdest du origins="https://deine-domain.com" setzen,
 * nicht "*" — aber für Entwicklung ist * in Ordnung.
 */
public class VersandController {

    /*
     * Hier wird der VersandService injiziert.
     * "final" + @RequiredArgsConstructor = Konstruktor-Injection.
     *
     * Warum nicht @Autowired direkt auf das Feld?
     * Konstruktor-Injection ist besser weil:
     * 1. Das Objekt kann nicht ohne seine Dependencies existieren
     * 2. Einfacher zu testen (du kannst im Test einen Mock übergeben)
     * 3. Spring selbst empfiehlt es seit Version 4.3
     *
     * Den VersandService erstellen wir in Schritt 4 —
     * bis dahin zeigt IntelliJ hier einen roten Fehler. Das ist normal.
     */
    private final com.jurabek.versand.service.VersandService versandService;

    /**
     * POST /api/v1/versand/vergleichen
     *
     * Empfängt eine VersandAnfrage und gibt eine Liste von
     * VersandAngeboten aller Anbieter zurück.
     *
     * Warum POST statt GET?
     * GET-Parameter stehen in der URL — ungeeignet für komplexe Objekte
     * wie unsere VersandAnfrage mit 6 Feldern.
     * POST schickt die Daten im Request-Body — sicherer und sauberer.
     *
     * @RequestBody  — Spring liest den JSON aus dem HTTP-Body
     *                 und konvertiert ihn automatisch in ein VersandAnfrage-Objekt.
     *                 Das macht Jackson (ist in spring-boot-starter-web enthalten).
     *
     * @Valid        — Aktiviert die Validierung die wir in VersandAnfrage
     *                 mit @NotNull, @Positive etc. definiert haben.
     *                 Ohne @Valid werden die Annotationen ignoriert!
     *
     * ResponseEntity<List<VersandAngebot>> — wir geben nicht nur die Daten zurück,
     * sondern auch den HTTP-Status-Code. Beispiele:
     *   200 OK        — alles gut
     *   400 Bad Request — ungültige Eingabe
     *   500 Internal Server Error — unser Fehler
     */
    @PostMapping("/vergleichen")
    public ResponseEntity<List<VersandAngebot>> vergleichen(
            @Valid @RequestBody VersandAnfrage anfrage) {

        // Logging — wer hat was angefragt?
        // In echten Projekten niemals persönliche Daten loggen (DSGVO!)
        // Gewicht und Maße sind aber unkritisch.
        log.info("Neue Versandanfrage: {}kg, {}x{}x{}cm, von {} nach {}",
                anfrage.getGewichtKg(),
                anfrage.getLaengeCm(),
                anfrage.getBreiteCm(),
                anfrage.getHoeheCm(),
                anfrage.getAbsenderland(),
                anfrage.getZielland());

        // Service aufrufen — er macht die eigentliche Arbeit
        List<VersandAngebot> angebote = versandService.vergleichen(anfrage);

        // HTTP 200 OK + die Liste als JSON zurückschicken
        return ResponseEntity.ok(angebote);
    }

    /**
     * GET /api/v1/versand/health
     *
     * Ein einfacher Health-Check Endpoint.
     * Warum? Monitoring-Systeme und Load-Balancer fragen regelmäßig:
     * "Lebt dieser Service noch?" — dieser Endpoint antwortet darauf.
     * In echten Projekten macht das Spring Actuator automatisch,
     * aber für unser Projekt zeigt es dass du das Konzept kennst.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.debug("Health-Check aufgerufen");
        return ResponseEntity.ok("Versandkosten-Service läuft");
    }
}