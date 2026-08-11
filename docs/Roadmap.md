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

- [x] Introduce biome path profiles without coupling them to the wear storage engine
- [x] Add data-driven forest-biome detection using biome tags
- [x] Integrate Rooted Dirt into forest-path progression
- [x] Integrate Podzol more strongly into forest variants
- [x] Add damp/vegetated biome profile
- [x] Integrate Moss and Mossy Cobblestone into suitable damp paths
- [x] Add Mud as a wearable starting block
- [x] Add Packed Mud as a compacted intermediate stage before drier path materials
- [x] Keep open-landscape paths close to the 0.1.0 progression
- [x] Avoid forcing every block in a biome through exactly the same sequence
- [x] Keep Farmland protected in every biome profile
- [x] Validate biome transitions in-game

### Damp-path defaults

- Mud: 50 crossings → Packed Mud
- Packed Mud: 75 additional crossings → Moss Block or Rooted Dirt in damp biomes
- Outside damp biomes, Packed Mud progresses to Coarse Dirt
- Damp paths can later end in Cobblestone or Mossy Cobblestone depending on the stable block-position variation

## 0.3.0 – Organic Paths

- [x] Treat path centres and edges differently
- [x] Let heavily travelled tracks wear down more strongly
- [x] Allow Moss to develop more often along lightly travelled edges
- [x] Create natural-looking path widths instead of single uniform block lines
- [x] Introduce controlled variation so neighbouring path blocks do not always match
- [x] Keep path generation deterministic enough to avoid visual flicker or repeated rerolls

### Current organic-path behaviour

- The directly travelled block receives full wear and remains the dominant track centre.
- Roughly one quarter of travelled positions contribute one additional wear point to a neighbouring shoulder.
- Whether a position creates shoulder wear and which side receives it are derived from the block position, producing stable narrow and wide sections.
- Walking the same route in the opposite direction selects the same shoulder rather than mirroring it.
- Large position jumps and teleports do not create edge wear.
- Edge-dominated wear can favour Moss and Mossy Cobblestone in damp biomes.
- Edge wear uses the same protected-block and biome-aware progression rules as direct wear.
- Adjacent walkable surfaces one block above or below the path can receive shoulder wear.
- Wear, edge-wear values and shoulder placement were validated across world reloads and remained stable.

## 0.4.0 – Entity Traffic

- [x] Allow selected ground-based vanilla mobs to contribute to wear while excluding animals
- [x] Track entity movement efficiently without scanning every entity globally
- [x] Evaluate different wear weights by entity type and size
- [x] Keep vehicles/passengers from accidentally double-counting traffic
- [x] Validate performance with many nearby entities

### Current entity-traffic behaviour

- Villagers, Wandering Traders and other ground-based vanilla non-animal mobs contribute to path wear.
- Animals are excluded so enclosures and grazing areas do not turn into paths.
- Normal mobs contribute one wear point per valid ground-block crossing.
- Iron Golems, Ravagers and Wardens contribute two wear points per valid crossing.
- Still entities, passengers, vehicles, teleports and large position jumps do not create artificial wear.
- Entity traffic was validated with the debug HUD; tracked entity counts remained stable and wear stopped when mob AI was disabled.

## 0.5.0 – MineColonies Integration

- [x] Detect MineColonies Citizens when MineColonies is installed
- [x] Let Citizens contribute naturally to Living Paths wear
- [x] Keep MineColonies an optional integration rather than a hard dependency
- [x] Evaluate role- or traffic-based weighting only if it improves natural path formation
- [x] Validate common colony routes such as Builder, Warehouse, Residence, Mine and Farm traffic

### Current MineColonies behaviour

- MineColonies Citizens are detected through the registered `minecolonies:citizen` entity type without importing MineColonies classes.
- The integration remains optional and Living Paths also starts normally when MineColonies is not installed.
- Citizens contribute one wear point per valid ground-block crossing and use the same biome-aware centre and shoulder rules as other traffic.
- Citizen professions do not receive artificial wear bonuses; frequently travelled work routes become stronger through their actual traffic.
- Citizen traffic was validated in-game on colony routes; crossings increased while Citizens moved and stopped when they stood still.

## 0.6.0 – Configuration & Long-Term Behaviour

- [x] Make wear thresholds configurable
- [x] Make wear decay configurable
- [x] Make protected blocks configurable while keeping Farmland protected by default
- [x] Make entity traffic configurable
- [x] Evaluate regeneration or overgrowth of rarely used established paths
- [x] Evaluate rare further development of extremely heavily used Cobblestone paths
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
