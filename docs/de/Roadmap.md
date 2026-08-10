# Living Paths – Roadmap

## 0.1.0 – Basic Path Wear

- [x] Spielerbewegung serverseitig erkennen
- [x] Betretene Bodenblöcke erfassen
- [x] Nutzung pro Blockposition zählen
- [x] Einen Block nur beim tatsächlichen Wechsel auf einen neuen Bodenblock zählen
- [x] Grass Block verschleissen lassen
- [x] Grundkette umsetzen:
  - Grass Block
  - Dirt Path
  - Coarse Dirt
  - Gravel
  - Cobblestone
- [x] Podzol unterstützen
- [x] Mycelium unterstützen
- [x] Podzol und Mycelium deutlich langsamer verschleissen lassen
- [x] Farmland vollständig von Veränderungen ausschliessen
- [x] Verschleissdaten persistent speichern
- [x] Alte Verschleissdaten abbauen und entfernen, damit Weltspeicher nicht unbegrenzt wachsen
- [x] Multiplayer-taugliche serverseitige Verarbeitung implementieren
- [x] Performance bei normalem Spielen prüfen
- [x] Erste Testschwellen im Spiel prüfen und als erste Standardwerte beibehalten

### Erste Standard-Schwellen

- 25 Übertritte: Grass Block → Dirt Path
- weitere 50 Übertritte: Dirt Path → Coarse Dirt
- weitere 100 Übertritte: Coarse Dirt → Gravel
- weitere 200 Übertritte: Gravel → Cobblestone
- Podzol/Mycelium: ungefähr Faktor 3 gegenüber Grass Blocks

### Hinweis zur Validierung

Die serverseitige Implementierung ist so aufgebaut, dass mehrere Spieler unabhängig zum selben Verschleisswert eines Weges beitragen können. Ein echter Zwei-Spieler-Test ist bei Gelegenheit weiterhin sinnvoll, wird für 0.1.0 aber nicht als Blocker betrachtet.

## 0.2.0 – Biome Paths

- [ ] Biom-Wegprofile einführen, ohne sie mit der Verschleiss-Speicherlogik zu vermischen
- [ ] Wald-Biome datengetrieben über Biome-Tags erkennen
- [ ] Rooted Dirt in die Waldweg-Entwicklung integrieren
- [ ] Podzol stärker in Waldvarianten integrieren
- [ ] Profil für feuchte/stark bewachsene Biome ergänzen
- [ ] Moss und Mossy Cobblestone in geeignete feuchte Wege integrieren
- [ ] Offene Landschaft nahe an der 0.1.0-Grundkette halten
- [ ] Nicht jeden Block eines Bioms zwingend durch exakt dieselbe Kette schicken
- [ ] Farmland in allen Biomprofilen geschützt lassen
- [ ] Biomübergänge im Spiel testen

## 0.3.0 – Organic Paths

- [ ] Wegmitte und Randbereiche unterschiedlich behandeln
- [ ] Stark genutzte Laufspuren stärker verschleissen lassen
- [ ] Moos eher an wenig genutzten Rändern entstehen lassen
- [ ] Natürlich wirkende Wegbreiten statt einzelner gleichförmiger Blocklinien erzeugen
- [ ] Kontrollierte Variation einführen, damit benachbarte Wegblöcke nicht immer identisch sind
- [ ] Wegentwicklung ausreichend deterministisch halten, damit keine sichtbaren Neuwürfe entstehen

## 0.4.0 – Entity Traffic

- [ ] Ausgewählte Vanilla-Mobs und Tiere zum Verschleiss beitragen lassen
- [ ] Entitätsbewegung effizient erfassen, ohne global alle Entitäten zu scannen
- [ ] Unterschiedliche Gewichtung nach Entitätstyp und Grösse prüfen
- [ ] Fahrzeuge/Passagiere nicht versehentlich doppelt zählen
- [ ] Performance mit vielen Entitäten in der Nähe prüfen

## 0.5.0 – MineColonies Integration

- [ ] MineColonies Citizens erkennen, wenn MineColonies installiert ist
- [ ] Citizens natürlich zum Living-Paths-Verschleiss beitragen lassen
- [ ] MineColonies als optionale Integration statt harte Abhängigkeit umsetzen
- [ ] Rollen-/Verkehrsgewichtung nur einführen, wenn sie natürlichere Wege erzeugt
- [ ] Typische Kolonie-Routen wie Builder, Warehouse, Residence, Mine und Farm testen

## 0.6.0 – Configuration & Long-Term Behaviour

- [ ] Verschleiss-Schwellen konfigurierbar machen
- [ ] Wear-Decay konfigurierbar machen
- [ ] Geschützte Blöcke konfigurierbar machen, Farmland aber standardmässig geschützt lassen
- [ ] Entitätsverkehr konfigurierbar machen
- [ ] Regeneration bzw. Überwachsen wenig genutzter etablierter Wege prüfen
- [ ] Seltene Weiterentwicklung extrem stark genutzter Cobblestone-Wege prüfen
- [ ] Sinnvolle Standardwerte bereitstellen, sodass für normale Nutzung keine Konfiguration nötig ist

## Richtung 1.0.0

- [ ] Langzeit-Test in einer Survival-Welt
- [ ] Echter Multiplayer-Test, sobald möglich
- [ ] Kompatibilitätsdurchgang mit dem geplanten Modpack
- [ ] Finaler Performance- und Speichergrössen-Check
- [ ] Finale Prüfung der englischen und deutschen Lokalisierung
- [ ] Veröffentlichungsfertige Dokumentation und Paketierung

## Grundsatz

Living Paths soll Nutzung sichtbar machen, ohne wie ein automatischer Strassenbauer zu wirken. Die Landschaft soll sich aus tatsächlicher Bewegung entwickeln und dabei möglichst natürlich aussehen.
