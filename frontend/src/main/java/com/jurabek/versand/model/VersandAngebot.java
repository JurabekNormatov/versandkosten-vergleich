package com.jurabek.versand.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * VersandAngebot repräsentiert das Angebot eines einzelnen Anbieters.
 * Diese Klasse wird vom Backend an das Frontend zurückgeschickt.
 *
 * Hier nutzen wir drei Lombok-Annotationen:
 *
 * @Data          — Getter, Setter, toString, equals, hashCode (wie vorher)
 * @Builder       — Ermöglicht das Builder-Pattern (siehe unten)
 * @NoArgsConstructor — Konstruktor ohne Parameter (von JPA und Jackson gebraucht)
 * @AllArgsConstructor — Konstruktor mit allen Parametern (vom Builder gebraucht)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersandAngebot {

    /**
     * Name des Versandanbieters.
     * Mögliche Werte: "DHL", "Hermes", "DPD"
     */
    private String anbieter;

    /**
     * Preis in Euro.
     * BigDecimal wäre in echten Finanzanwendungen besser (kein Rundungsfehler),
     * aber für unser Portfolio-Projekt ist Double ausreichend.
     */
    private Double preisEuro;

    /**
     * Voraussichtliche Lieferdauer in Werktagen.
     * Kann null sein — nicht alle APIs liefern diese Information.
     */
    private Integer lieferdauerWerktage;

    /**
     * Gibt an ob dieses Angebot das günstigste ist.
     * Wird im Service berechnet, nicht von der externen API.
     * Das Frontend nutzt dieses Flag um das Angebot hervorzuheben.
     */
    private boolean guenstigster;

    /**
     * Fehlermeldung falls ein Anbieter nicht erreichbar war.
     * Beispiel: "DHL API nicht verfügbar"
     * Wenn null — alles hat geklappt.
     */
    private String fehler;
}