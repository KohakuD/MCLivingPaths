# Living Paths – Roadmap

## 0.1.0 – Basic Path Wear

- [x] Detect player movement server-side
- [x] Detect the ground blocks players walk on
- [x] Count usage per block position
- [x] Count a block only when the player actually moves onto a new ground block
- [x] Make Grass Blocks wear down
- [x] Implement the basic progression:
  - Grass Block
  - Dirt Path
  - Coarse Dirt
  - Gravel
  - Cobblestone
- [x] Support Podzol
- [x] Support Mycelium
- [x] Make Podzol and Mycelium wear down significantly more slowly
- [x] Completely exclude Farmland from changes
- [x] Store wear data persistently
- [x] Decay and remove stale wear data so world saves do not grow indefinitely
- [x] Implement multiplayer-safe server-side processing
- [x] Check performance during normal gameplay
- [x] Test the initial thresholds in-game and keep them as the first defaults

### Initial Default Thresholds

- 25 crossings: Grass Block → Dirt Path
- 50 additional crossings: Dirt Path → Coarse Dirt
- 100 additional crossings: Coarse Dirt → Gravel
- 200 additional crossings: Gravel → Cobblestone
- Podzol/Mycelium: approximately 3× the resistance of Grass Blocks

### Validation Note

The server-side implementation is designed so multiple players can contribute to the same path wear data independently. A real two-player validation test is still desirable when possible, but it is not considered a blocker for 0.1.0.

## Planned Development

### Biome-Dependent Paths

- Rooted Dirt in suitable forest biomes
- Integrate Podzol more strongly into forest variants
- Moss and Mossy Cobblestone in damp or heavily vegetated areas
- Different path characteristics depending on biome
- More natural, non-fully-linear transitions

### Organic Path Structure

- Treat path centres and edges differently
- Let heavily travelled tracks wear down more strongly
- Allow Moss to develop more often along lightly travelled edges
- Create natural-looking path widths instead of single uniform block lines

### Additional Entities

- Vanilla mobs and animals
- MineColonies Citizens
- Evaluate different weighting depending on entity type

### Configuration and Long-Term Behaviour

- Make wear speed configurable
- Make protected blocks configurable
- Evaluate regeneration or overgrowth of rarely used paths
- Evaluate rare further development of extremely heavily used Cobblestone paths

## Guiding Principle

Living Paths should make traffic visible without behaving like an automatic road builder. The landscape should evolve from actual movement and remain as natural-looking as possible.
