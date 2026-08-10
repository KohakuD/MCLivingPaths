# Living Paths – Roadmap

## 0.1.0 – Basic Path Wear

- [ ] Spielerbewegung serverseitig erkennen
- [ ] Betretene Bodenblöcke erfassen
- [ ] Nutzung pro Blockposition zählen
- [ ] Einen Block nur beim tatsächlichen Wechsel auf einen neuen Bodenblock zählen
- [ ] Grass Block verschleissen lassen
- [ ] Grundkette umsetzen:
  - Grass Block
  - Dirt Path
  - Coarse Dirt
  - Gravel
  - Cobblestone
- [ ] Podzol unterstützen
- [ ] Mycelium unterstützen
- [ ] Podzol und Mycelium deutlich langsamer verschleissen lassen
- [ ] Farmland vollständig von Veränderungen ausschliessen
- [ ] Verschleissdaten persistent speichern
- [ ] Multiplayer-taugliche serverseitige Verarbeitung sicherstellen
- [ ] Performance bei normalem Spielen prüfen
- [ ] Erste Testschwellen im Spiel prüfen und bei Bedarf anpassen

### Erste Testschwellen

- 25 Übertritte: Grass Block → Dirt Path
- weitere 50 Übertritte: Dirt Path → Coarse Dirt
- weitere 100 Übertritte: Coarse Dirt → Gravel
- weitere 200 Übertritte: Gravel → Cobblestone
- Podzol/Mycelium: zunächst ungefähr Faktor 3 gegenüber Grass Blocks

## Geplante Weiterentwicklung

### Biomabhängige Wege

- Rooted Dirt in geeigneten Wald-Biomen
- Podzol stärker in Waldvarianten integrieren
- Moss und Mossy Cobblestone in feuchten oder bewachsenen Gebieten
- unterschiedliche Wegcharaktere je nach Biom
- natürlichere, nicht vollständig lineare Übergänge

### Organische Wegstruktur

- Wegmitte und Randbereiche unterschiedlich behandeln
- stark genutzte Laufspuren stärker verschleissen lassen
- Moos eher an wenig genutzten Rändern entstehen lassen
- natürlich wirkende Wegbreiten statt einzelner gleichförmiger Blocklinien

### Weitere Entitäten

- Vanilla-Mobs und Tiere
- MineColonies Citizens
- unterschiedliche Gewichtung je Entitätstyp prüfen

### Konfiguration und Langzeitverhalten

- Verschleissgeschwindigkeit konfigurierbar machen
- geschützte Blöcke konfigurierbar machen
- Regeneration bzw. Überwachsen wenig genutzter Wege prüfen
- seltene Weiterentwicklung extrem stark genutzter Cobblestone-Wege prüfen

## Grundsatz

Living Paths soll Nutzung sichtbar machen, ohne wie ein automatischer Strassenbauer zu wirken. Die Landschaft soll sich aus tatsächlicher Bewegung entwickeln und dabei möglichst natürlich aussehen.
