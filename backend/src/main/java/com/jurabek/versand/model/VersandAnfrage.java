package com.jurabek.versand.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * VersandAnfrage repräsentiert die Eingabe des Nutzers.
 *
 * @Data von Lombok generiert automatisch:
 *   - Getter für alle Felder (getGewichtKg(), getLaengeCm(), ...)
 *   - Setter für alle Felder
 *   - toString() — für Logging
 *   - equals() und hashCode() — für Vergleiche
 *
 * Ohne Lombok wären das ~60 Zeilen extra Code.
 */
@Data
public class VersandAnfrage {

    /**
     * Gewicht des Pakets in Kilogramm.
     *
     * @NotNull  — darf nicht fehlen (null ist verboten)
     * @Positive — muss größer als 0 sein (0.0 ist verboten)
     *
     * Warum Double statt int?
     * Pakete können 1.5 kg wiegen — wir brauchen Dezimalzahlen.
     */
    @NotNull(message = "Gewicht darf nicht leer sein")
    @Positive(message = "Gewicht muss größer als 0 sein")
    private Double gewichtKg;

    /**
     * Maße des Pakets in Zentimetern.
     * Alle drei Maße sind für die Preisberechnung notwendig —
     * viele Anbieter berechnen "Volumenwicht" (Länge × Breite × Höhe / 5000).
     */
    @NotNull(message = "Länge darf nicht leer sein")
    @Positive(message = "Länge muss größer als 0 sein")
    private Double laengeCm;

    @NotNull(message = "Breite darf nicht leer sein")
    @Positive(message = "Breite muss größer als 0 sein")
    private Double breiteCm;

    @NotNull(message = "Höhe darf nicht leer sein")
    @Positive(message = "Höhe muss größer als 0 sein")
    private Double hoeheCm;

    /**
     * Absender- und Zielland als ISO-Ländercode.
     * Beispiele: "DE" für Deutschland, "FR" für Frankreich, "US" für USA
     *
     * @NotBlank — darf nicht null UND nicht leer ("") UND nicht nur Leerzeichen sein.
     * Unterschied zu @NotNull: @NotNull erlaubt "", @NotBlank nicht.
     */
    @NotBlank(message = "Absenderland darf nicht leer sein")
    private String absenderland;

    @NotBlank(message = "Zielland darf nicht leer sein")
    private String zielland;

    /**
     * Optionale PLZ des Empfängers.
     * Manche Anbieter berechnen Preise nach PLZ-Zonen.
     * Optional = kein @NotNull, kann null sein.
     */
    private String empfaengerPlz;
}