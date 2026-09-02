# Living Paths – Design

## Grundidee

Living Paths verändert die Landschaft abhängig davon, wie häufig Bodenblöcke von Spielern und ausgewählten Entitäten überquert werden.

Wege entstehen aus tatsächlichem Verkehr, entwickeln sich organisch und variieren abhängig von Biom und Position, statt gleichförmige automatische Strassen zu bilden.

## Grundregeln

### Standard-Abnutzung

Die Standardentwicklung in offenen Landschaften lautet:

Grass Block
→ Dirt Path
→ Coarse Dirt
→ Gravel
→ Cobblestone
→ Stone
→ Smooth Stone

Normale Dirt-Blöcke werden bewusst nicht als Abnutzungsstufe verwendet, da sie durch benachbarte Grass- oder Mycelium-Blöcke wieder überwachsen können.

Stone und Smooth Stone sind seltene späte Stufen. Nur von Living Paths erzeugter, etablierter Cobblestone kann sich zu Stone entwickeln; natürlich vorkommender und von Spielern platzierter Cobblestone wird nicht umgewandelt.

### Geschützte Blöcke

Konfigurierte geschützte Blöcke werden weder abgenutzt noch ersetzt. Farmland ist standardmässig in der Schutzliste enthalten. Serveradministratoren können diese Liste mit Block-IDs einschliesslich Namensraum erweitern oder ändern.

### Podzol und Mycelium

Podzol und Mycelium können verschleissen, verwenden aber standardmässig eine Schwelle von 75 Übertritten und benötigen damit dreimal so viel Verkehr wie Grass Block. Beim Erreichen der Schwelle entwickeln sich beide zu Dirt Path.

### Biomabhängige Entwicklung

Living Paths wählt für jede Position ein datengetriebenes Wegprofil:

- Waldgebiete können Podzol und Rooted Dirt einführen.
- Feuchte oder stark bewachsene Gebiete können Mud, Packed Mud, Moss, Rooted Dirt und Mossy Cobblestone einführen.
- Offene Landschaften bleiben nahe an der Standardentwicklung über Dirt Path, Coarse Dirt und Gravel.

Eine stabile, aus der Blockposition abgeleitete Variation verhindert, dass jeder Block eines Bioms exakt dieselbe Abfolge durchläuft. Dadurch entsteht eine Mischung unterschiedlich stark abgenutzter Materialien, die auch nach einem Weltneustart stabil bleibt.

### Natürliches Erscheinungsbild

Der direkt betretene Block erhält den vollständigen Verschleiss der Wegmitte. Zwei von fünf betretenen Positionen geben zusätzlich Verschleiss an eine benachbarte Wegschulter.

Die Wegschulter wird aus Blockposition und Bewegungsrichtung so bestimmt, dass sie nach Weltneustarts stabil bleibt und beim Begehen der Strecke in Gegenrichtung dieselbe Seite gewählt wird. Geeignete Oberflächen auf gleicher Höhe sowie einen Block höher oder tiefer können Randverschleiss erhalten.

Randdominierter Verschleiss bevorzugt in feuchten Biomen Moss und Mossy Cobblestone. Teleports und grössere Positionssprünge erzeugen keinen Randverschleiss.

### Entitätsverkehr

Ausgewählte bodengebundene Vanilla-Nichttier-Mobs tragen standardmässig zum Verschleiss bei. Tiere sind zum Schutz von Gehegen und Weideflächen standardmässig deaktiviert, können aber in der Konfiguration aktiviert werden.

Normale Mobs erzeugen standardmässig einen Verschleisspunkt pro gültigem Übertritt. Eisengolems, Verwüster und Wärter erzeugen zwei. Stillstehende Entitäten, Passagiere, Fahrzeuge, Teleports und grosse Positionssprünge erzeugen keinen künstlichen Verkehr.

MineColonies Citizens werden über ihren registrierten Entitätstyp `minecolonies:citizen` erkannt. Dadurch bleibt MineColonies optional und es entsteht keine feste Code-Abhängigkeit. Citizens verwenden dieselben biomabhängigen Regeln für Wegmitte und Rand wie anderer Verkehr.

Der Player-Two-Begleiter Nox wird über den registrierten Entitätstyp `playertwo:nox` erkannt. Player Two bleibt optional; der Nox-Verkehr besitzt einen eigenen Schalter und ein konfigurierbares Verschleissgewicht.

### Verschleissabbau und Regeneration

Gespeicherter Verschleiss an einer inaktiven Position sinkt standardmässig um einen Punkt pro Minecraft-Tag. Dadurch wachsen unvollständige Verschleissdaten nicht unbegrenzt.

Von Living Paths erzeugte etablierte Wege regenerieren standardmässig nach 30 inaktiven Minecraft-Tagen langsam um jeweils eine Stufe. Natürlich vorkommende und von Spielern platzierte Wegmaterialien werden nicht für die Regeneration registriert.

Die Regeneration führt etablierte Wege über passende frühere Stufen zurück, zum Beispiel Smooth Stone → Stone → Cobblestone → Gravel → Coarse Dirt → Dirt Path → Grass Block. Feuchte Varianten verwenden passende Rückstufen wie Mossy Cobblestone → Moss Block → Grass Block.

## Technische Grundprinzipien

- Die gesamte Verschleissverarbeitung und alle persistenten Zustandsänderungen erfolgen serverseitig.
- Spielerverschleiss wird nur beim Wechsel auf einen neuen Bodenblock gezählt; ausgewählte Entitäten tragen nur bei gültigen benachbarten Übertritten bei.
- Nutzung, Randverschleiss, letzte Aktivität und der Zustand etablierter Wege werden pro Blockposition erfasst.
- Verschleissdaten werden persistent gespeichert und bleiben nach Weltneustarts stabil.
- Die Spieler- und Entitätserfassung benötigt keinen globalen Scan aller Entitäten.
- Schutzregeln und Biomprofile gelten gleichermassen für Wegmitte und Randverschleiss.
- Die Unterstützung für MineColonies und Player Two bleibt optional und importiert keine Klassen aus den beiden Mods.
- Schwellenwerte, Verschleissabbau, Regeneration, geschützte Blöcke, Verkehrsschalter und Entitätsgewichte sind konfigurierbar.

## Standardschwellen für 0.6.0

Jede Schwelle bezeichnet den zusätzlichen Verkehr, der innerhalb der jeweiligen Blockstufe nötig ist:

- Grass Block: 25 Übertritte → Dirt Path oder biomabhängige Alternative
- Mud: 50 Übertritte → Packed Mud
- Packed Mud: 75 Übertritte → biomabhängiges verdichtetes Wegmaterial
- Podzol: 75 Übertritte → Dirt Path
- Mycelium: 75 Übertritte → Dirt Path
- Dirt Path: 50 Übertritte → biomabhängiges etabliertes Wegmaterial
- Moss Block: 75 Übertritte → Rooted Dirt
- Rooted Dirt: 75 Übertritte → Coarse Dirt
- Coarse Dirt: 100 Übertritte → Gravel
- Gravel: 200 Übertritte → Cobblestone oder Mossy Cobblestone
- Von Living Paths erzeugter etablierter Cobblestone: 1.000 Übertritte → Stone
- Stone aus einem Living-Paths-Weg: 1.000 Übertritte → Smooth Stone

Diese Werte sind konfigurierbare Standards, die ohne verpflichtende Einrichtung funktionieren sollen. Spieltests und Langzeitprüfungen in Survival-Welten können vor `1.0.0` noch zu Balanceänderungen führen.
