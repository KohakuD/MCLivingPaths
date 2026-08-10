# Living Paths – Roadmap

## 0.1.0 – Basic Path Wear

- [ ] Detect player movement server-side
- [ ] Detect the ground blocks players walk on
- [ ] Count usage per block position
- [ ] Count a block only when the player actually moves onto a new ground block
- [ ] Make Grass Blocks wear down
- [ ] Implement the basic progression:
  - Grass Block
  - Dirt Path
  - Coarse Dirt
  - Gravel
  - Cobblestone
- [ ] Support Podzol
- [ ] Support Mycelium
- [ ] Make Podzol and Mycelium wear down significantly more slowly
- [ ] Completely exclude Farmland from changes
- [ ] Store wear data persistently
- [ ] Ensure multiplayer-safe server-side processing
- [ ] Check performance during normal gameplay
- [ ] Test the initial thresholds in-game and adjust them if necessary

### Initial Test Thresholds

- 25 crossings: Grass Block → Dirt Path
- 50 additional crossings: Dirt Path → Coarse Dirt
- 100 additional crossings: Coarse Dirt → Gravel
- 200 additional crossings: Gravel → Cobblestone
- Podzol/Mycelium: initially approximately 3× the resistance of Grass Blocks

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
