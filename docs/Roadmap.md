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

## 0.2.0 – Biome Paths

- [ ] Introduce biome path profiles without coupling them to the wear storage engine
- [ ] Add data-driven forest-biome detection using biome tags
- [ ] Integrate Rooted Dirt into forest-path progression
- [ ] Integrate Podzol more strongly into forest variants
- [ ] Add damp/vegetated biome profile
- [ ] Integrate Moss and Mossy Cobblestone into suitable damp paths
- [ ] Keep open-landscape paths close to the 0.1.0 progression
- [ ] Avoid forcing every block in a biome through exactly the same sequence
- [ ] Keep Farmland protected in every biome profile
- [ ] Validate biome transitions in-game

## 0.3.0 – Organic Paths

- [ ] Treat path centres and edges differently
- [ ] Let heavily travelled tracks wear down more strongly
- [ ] Allow Moss to develop more often along lightly travelled edges
- [ ] Create natural-looking path widths instead of single uniform block lines
- [ ] Introduce controlled variation so neighbouring path blocks do not always match
- [ ] Keep path generation deterministic enough to avoid visual flicker or repeated rerolls

## 0.4.0 – Entity Traffic

- [ ] Allow selected vanilla mobs and animals to contribute to wear
- [ ] Track entity movement efficiently without scanning every entity globally
- [ ] Evaluate different wear weights by entity type and size
- [ ] Keep vehicles/passengers from accidentally double-counting traffic
- [ ] Validate performance with many nearby entities

## 0.5.0 – MineColonies Integration

- [ ] Detect MineColonies Citizens when MineColonies is installed
- [ ] Let Citizens contribute naturally to Living Paths wear
- [ ] Keep MineColonies an optional integration rather than a hard dependency
- [ ] Evaluate role- or traffic-based weighting only if it improves natural path formation
- [ ] Validate common colony routes such as Builder, Warehouse, Residence, Mine and Farm traffic

## 0.6.0 – Configuration & Long-Term Behaviour

- [ ] Make wear thresholds configurable
- [ ] Make wear decay configurable
- [ ] Make protected blocks configurable while keeping Farmland protected by default
- [ ] Make entity traffic configurable
- [ ] Evaluate regeneration or overgrowth of rarely used established paths
- [ ] Evaluate rare further development of extremely heavily used Cobblestone paths
- [ ] Provide sensible defaults so no configuration is required for normal use

## Towards 1.0.0

- [ ] Long-term survival-world test
- [ ] Real multiplayer validation when possible
- [ ] Compatibility pass with the intended modpack
- [ ] Final performance and save-size review
- [ ] Final English and German localisation review
- [ ] Publication-ready documentation and packaging

## Guiding Principle

Living Paths should make traffic visible without behaving like an automatic road builder. The landscape should evolve from actual movement and remain as natural-looking as possible.
