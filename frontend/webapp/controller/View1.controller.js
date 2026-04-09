sap.ui.define([
    "sap/ui/core/mvc/Controller",
    "sap/m/MessageToast",
    "sap/m/StandardListItem",
    "sap/ui/model/json/JSONModel"
], (Controller, MessageToast, StandardListItem, JSONModel) => {
    "use strict";

    return Controller.extend("project1.controller.View1", {

        // ============================================
        // onInit — wird einmal beim Start aufgerufen
        // Hier initialisieren wir das Model für die View
        // ============================================
        onInit() {
            // JSONModel — ein einfaches Key-Value Datenmodell
            // Die View kann direkt auf diese Daten zugreifen
            // via {viewModel>/gewicht} usw.
            const oModel = new JSONModel({
                busy: false,        // zeigt Loading-Indikator
                ergebnisse: []      // leere Ergebnisliste am Anfang
            });
            this.getView().setModel(oModel, "viewModel");
        },

        // ============================================
        // onVergleichen — wird beim Button-Klick aufgerufen
        // ============================================
        onVergleichen() {
            // 1. Eingaben lesen
            const gewicht = this.byId("gewicht").getValue();
            const laenge  = this.byId("laenge").getValue();
            const breite  = this.byId("breite").getValue();
            const hoehe   = this.byId("hoehe").getValue();
            const absenderland = this.byId("absenderland").getSelectedKey();
            const zielland     = this.byId("zielland").getSelectedKey();

            // 2. Validierung — sind alle Felder ausgefüllt?
            if (!gewicht || !laenge || !breite || !hoehe) {
                MessageToast.show("Bitte alle Felder ausfüllen!");
                return; // Funktion hier beenden
            }

            // 3. Fehlerstreifen und Ergebnisse zurücksetzen
            this.byId("fehlerStrip").setVisible(false);
            this.byId("ergebnisPanel").setVisible(false);
            this.byId("ergebnisList").removeAllItems();

            // 4. Loading anzeigen
            this.byId("vergleichenBtn").setEnabled(false);
            this.byId("vergleichenBtn").setText("Wird geladen...");

            // 5. Request-Body aufbauen — genau wie unsere VersandAnfrage.java
            const requestBody = {
                gewichtKg:      parseFloat(gewicht),
                laengeCm:       parseFloat(laenge),
                breiteCm:       parseFloat(breite),
                hoeheCm:        parseFloat(hoehe),
                absenderland:   absenderland,
                zielland:       zielland
            };

            // 6. Backend aufrufen mit fetch()
            // fetch() ist die moderne Art HTTP-Requests zu machen
            // WICHTIG: Die URL muss zu deinem Spring Boot Backend zeigen
            // Später wenn beide deployed sind, änderst du die URL
            fetch("http://localhost:8080/api/v1/versand/vergleichen", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(requestBody)
            })
            .then(response => {
                // HTTP Status prüfen
                if (!response.ok) {
                    throw new Error("Server Fehler: " + response.status);
                }
                // JSON parsen
                return response.json();
            })
            .then(angebote => {
                // 7. Ergebnisse anzeigen
                this._zeigeErgebnisse(angebote);
            })
            .catch(error => {
                // 8. Fehler anzeigen
                console.error("Fehler:", error);
                this.byId("fehlerStrip").setText(
                    "Verbindung zum Server fehlgeschlagen. Läuft das Backend?"
                );
                this.byId("fehlerStrip").setVisible(true);
            })
            .finally(() => {
                // 9. Button immer wieder aktivieren — egal ob Erfolg oder Fehler
                this.byId("vergleichenBtn").setEnabled(true);
                this.byId("vergleichenBtn").setText("Preise vergleichen");
            });
        },

        // ============================================
        // _zeigeErgebnisse — private Hilfsfunktion
        // Konvention: _ am Anfang = private (nicht von außen aufrufen)
        // ============================================
        _zeigeErgebnisse(angebote) {
            const oList = this.byId("ergebnisList");

            // Für jedes Angebot ein Listen-Element erstellen
            angebote.forEach(angebot => {

                // Fehlerfall — Anbieter nicht verfügbar
                if (angebot.fehler) {
                    const oItem = new StandardListItem({
                        title: angebot.anbieter,
                        description: angebot.fehler,
                        icon: "sap-icon://error",
                        iconInset: false
                    });
                    oList.addItem(oItem);
                    return;
                }

                // Normaler Fall — Preis anzeigen
                const lieferdauer = angebot.lieferdauerWerktage
                    ? ` | Lieferung: ${angebot.lieferdauerWerktage} Werktage`
                    : "";

                const titel = angebot.guenstigster
                    ? `⭐ ${angebot.anbieter} — GÜNSTIGSTER`
                    : angebot.anbieter;

                const oItem = new StandardListItem({
                    title: titel,
                    description: `${angebot.preisEuro.toFixed(2)} €${lieferdauer}`,
                    highlight: angebot.guenstigster ? "Success" : "None",
                    iconInset: false
                });

                oList.addItem(oItem);
            });

            // Ergebnispanel einblenden
            this.byId("ergebnisPanel").setVisible(true);

            // Nach unten scrollen
            MessageToast.show("Vergleich abgeschlossen!");
        }
    });
});