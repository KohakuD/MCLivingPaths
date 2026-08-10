# Living Paths – Design

## Grundidee

Living Paths verändert die Landschaft abhängig davon, wie häufig bestimmte Blöcke von Spielern und später auch anderen Entitäten betreten werden.

Wege sollen organisch entstehen und nicht überall identisch aussehen.

## Grundregeln

### Standard-Abnutzung

Grass Block
→ Dirt Path
→ Coarse Dirt
→ Gravel
→ Cobblestone

Normale Dirt-Blöcke werden bewusst nicht als Abnutzungsstufe verwendet, da sie durch benachbarte Grass- oder Mycelium-Blöcke wieder überwachsen können.

### Geschützte Blöcke

Farmland wird niemals durch Living Paths verändert.

### Podzol und Mycelium

Podzol und Mycelium können ebenfalls verschleissen, sind jedoch deutlich widerstandsfähiger als Grass Blocks.

Für die erste Version sollen beide ungefähr den dreifachen Nutzungsaufwand gegenüber Grass Blocks benötigen. Dieser Wert ist ausdrücklich ein Testwert und kann nach Spieltests angepasst werden.

### Biomabhängige Entwicklung

Spätere Versionen sollen unterschiedliche Wegcharaktere abhängig vom Biom erzeugen.

Beispiele:

- Waldgebiete: Podzol und Rooted Dirt
- feuchte oder stark bewachsene Gebiete: Moss und Mossy Cobblestone
- offene Landschaft: Dirt Path, Coarse Dirt und Gravel

Biomabhängige Entwicklung soll nicht bedeuten, dass jeder Block dieselbe feste Kette durchläuft. Ziel ist eine natürliche Mischung aus stärker und schwächer beanspruchten Flächen.

### Höchste Verschleissstufe

Cobblestone ist zunächst die höchste reguläre Abnutzungsstufe.

Stone wird nicht automatisch als nächste Stufe verwendet. Eine spätere seltene Weiterentwicklung extrem stark genutzter Wege kann separat geprüft werden.

### Natürliches Erscheinungsbild

Langfristig sollen Wege nicht aus einer gleichmässigen Blockreihe bestehen. Die tatsächliche Nutzung soll Wegmitte, Randbereiche und unterschiedliche Verschleissgrade erzeugen.

Moos soll eher in wenig betretenen Randbereichen vorkommen als in der stark genutzten Wegmitte.

## Technische Grundprinzipien

- Die Verarbeitung erfolgt serverseitig.
- Ein Spieler erzeugt nicht bei jedem Game-Tick Nutzung, sondern nur beim tatsächlichen Wechsel auf einen neuen Bodenblock.
- Die Nutzung wird pro Blockposition gezählt.
- Verschleissdaten sollen persistent gespeichert werden.
- Farmland wird über eine geschützte Blockregel vollständig ausgeschlossen.
- Die Architektur soll spätere Unterstützung für Vanilla-Mobs und MineColonies Citizens ermöglichen.

## Erste Testschwellen für 0.1.0

Die folgenden Werte dienen nur als Ausgangspunkt für Spieltests:

- Grass Block: 25 Übertritte → Dirt Path
- Dirt Path: weitere 50 Übertritte → Coarse Dirt
- Coarse Dirt: weitere 100 Übertritte → Gravel
- Gravel: weitere 200 Übertritte → Cobblestone

Podzol und Mycelium sollen in 0.1.0 ungefähr Faktor 3 gegenüber Grass Blocks verwenden.
