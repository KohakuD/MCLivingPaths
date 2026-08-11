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

- [x] Biom-Wegprofile einführen, ohne sie mit der Verschleiss-Speicherlogik zu vermischen
- [x] Wald-Biome datengetrieben über Biome-Tags erkennen
- [x] Rooted Dirt in die Waldweg-Entwicklung integrieren
- [x] Podzol stärker in Waldvarianten integrieren
- [x] Profil für feuchte/stark bewachsene Biome ergänzen
- [x] Moss und Mossy Cobblestone in geeignete feuchte Wege integrieren
- [x] Mud als verschleissbaren Ausgangsblock ergänzen
- [x] Packed Mud als verdichtete Zwischenstufe vor trockeneren Wegmaterialien ergänzen
- [x] Offene Landschaft nahe an der 0.1.0-Grundkette halten
- [x] Nicht jeden Block eines Bioms zwingend durch exakt dieselbe Kette schicken
- [x] Farmland in allen Biomprofilen geschützt lassen
- [x] Biomübergänge im Spiel testen

### Standardwerte für feuchte Wege

- Mud: 50 Übertritte → Packed Mud
- Packed Mud: weitere 75 Übertritte → Moss Block oder Rooted Dirt in feuchten Biomen
- Ausserhalb feuchter Biome entwickelt sich Packed Mud zu Coarse Dirt
- Feuchte Wege können später abhängig von der stabilen Blockpositions-Variation in Cobblestone oder Mossy Cobblestone enden

## 0.3.0 – Organic Paths

- [x] Wegmitte und Randbereiche unterschiedlich behandeln
- [x] Stark genutzte Laufspuren stärker verschleissen lassen
- [x] Moos eher an wenig genutzten Rändern entstehen lassen
- [x] Natürlich wirkende Wegbreiten statt einzelner gleichförmiger Blocklinien erzeugen
- [x] Kontrollierte Variation einführen, damit benachbarte Wegblöcke nicht immer identisch sind
- [x] Wegentwicklung ausreichend deterministisch halten, damit keine sichtbaren Neuwürfe entstehen

### Aktuelles Verhalten organischer Wege

- Der direkt betretene Block erhält vollen Verschleiss und bleibt die dominante Wegmitte.
- Ungefähr ein Viertel der betretenen Positionen gibt zusätzlich einen Verschleisspunkt an eine benachbarte Wegschulter.
- Ob eine Position Randverschleiss erzeugt und welche Seite betroffen ist, wird aus der Blockposition abgeleitet. So entstehen stabile schmalere und breitere Abschnitte.
- Beim Begehen derselben Strecke in Gegenrichtung wird dieselbe Wegschulter gewählt, statt die Seite zu spiegeln.
- Grössere Positionssprünge und Teleports erzeugen keinen Randverschleiss.
- Randdominierter Verschleiss kann in feuchten Biomen Moss und Mossy Cobblestone bevorzugen.
- Randverschleiss verwendet dieselben Schutzregeln und biomabhängigen Entwicklungsregeln wie direkter Verschleiss.
- Begehbare Nachbaroberflächen eine Blockstufe höher oder tiefer können ebenfalls Randverschleiss erhalten.
- Wear-, Edge-Wear-Werte und die Position der Wegschultern wurden über einen Welt-Neustart hinweg geprüft und blieben stabil.

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

- [x] Verschleiss-Schwellen konfigurierbar machen
- [x] Wear-Decay konfigurierbar machen
- [x] Geschützte Blöcke konfigurierbar machen, Farmland aber standardmässig geschützt lassen
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
