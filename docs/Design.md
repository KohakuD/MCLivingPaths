# Living Paths – Design

## Core Idea

Living Paths changes the landscape based on how frequently ground blocks are crossed by players and selected entities.

Paths emerge from actual traffic, develop organically and vary with their biome and position instead of becoming uniform automatic roads.

## Core Rules

### Standard Wear Progression

The standard open-landscape progression is:

Grass Block
→ Dirt Path
→ Coarse Dirt
→ Gravel
→ Cobblestone
→ Stone
→ Smooth Stone

Regular Dirt is deliberately excluded as a wear stage because nearby Grass Blocks or Mycelium can spread back onto it.

Stone and Smooth Stone are rare late stages. Only established Cobblestone created by Living Paths can progress to Stone; naturally occurring and player-placed Cobblestone is not converted.

### Protected Blocks

Configured protected blocks are never worn or replaced. Farmland is included in the protected-block list by default. Server administrators can extend or change this list with namespaced block IDs.

### Podzol and Mycelium

Podzol and Mycelium can wear down but use a default threshold of 75 crossings, three times the Grass Block threshold. Both progress to Dirt Path when their threshold is reached.

### Biome-Dependent Development

Living Paths selects a data-driven path profile for each position:

- Forest areas can introduce Podzol and Rooted Dirt.
- Damp or heavily vegetated areas can introduce Mud, Packed Mud, Moss, Rooted Dirt and Mossy Cobblestone.
- Open landscapes remain close to the standard Dirt Path, Coarse Dirt and Gravel progression.

Stable block-position variation prevents every block in a biome from following exactly the same sequence. The result is a mixture of more and less heavily worn materials that remains stable across reloads.

### Natural Appearance

The directly travelled block receives full centre wear. Two out of five travelled positions also contribute wear to one neighbouring shoulder.

Shoulder selection is derived from the block position and travel direction in a way that remains stable across reloads and selects the same side when the route is travelled in reverse. Suitable surfaces on the same level or one block above or below can receive shoulder wear.

Edge-dominated wear favours Moss and Mossy Cobblestone in damp biomes. Teleports and larger position jumps do not create shoulder wear.

### Entity Traffic

Selected ground-based vanilla non-animal mobs contribute to wear by default. Animals are disabled by default to protect enclosures and grazing areas but can be enabled in the configuration.

Normal mobs contribute one wear point per valid crossing. Iron Golems, Ravagers and Wardens contribute two by default. Still entities, passengers, vehicles, teleports and large position jumps do not generate artificial traffic.

MineColonies Citizens are detected by their registered `minecolonies:citizen` entity type. This keeps MineColonies optional and avoids a hard code dependency. Citizens use the same biome-aware centre and shoulder rules as other traffic.

The Player Two companion Nox is detected by the registered `playertwo:nox` entity type. Player Two remains optional, and Nox traffic has its own enable switch and configurable wear weight.

### Wear Decay and Regeneration

Stored wear at an inactive position decays by one point per Minecraft day by default, preventing unfinished wear data from growing indefinitely.

Established paths created by Living Paths regenerate slowly after 30 inactive Minecraft days per stage by default. Naturally occurring and player-placed path materials are not registered for regeneration.

Regeneration moves established paths back through appropriate earlier stages, for example Smooth Stone → Stone → Cobblestone → Gravel → Coarse Dirt → Dirt Path → Grass Block. Damp variants use suitable return stages such as Mossy Cobblestone → Moss Block → Grass Block.

## Technical Principles

- All wear processing and persistent state changes happen server-side.
- Player wear is counted only when the player moves onto a new ground block; selected entities contribute only on valid adjacent crossings.
- Usage, edge wear, last activity and established-path state are tracked per block position.
- Wear data is stored persistently and remains stable across world reloads.
- Player and entity tracking avoids global entity scans.
- Protected-block and biome-profile rules apply equally to centre and shoulder wear.
- MineColonies and Player Two support remain optional and do not import classes from either mod.
- Thresholds, decay, regeneration, protected blocks, traffic switches and entity weights are configurable.

## Default Thresholds for 0.6.0

Each threshold is the additional traffic required while the block is in that stage:

- Grass Block: 25 crossings → Dirt Path or biome-specific alternative
- Mud: 50 crossings → Packed Mud
- Packed Mud: 75 crossings → biome-specific compacted path material
- Podzol: 75 crossings → Dirt Path
- Mycelium: 75 crossings → Dirt Path
- Dirt Path: 50 crossings → biome-specific established path material
- Moss Block: 75 crossings → Rooted Dirt
- Rooted Dirt: 75 crossings → Coarse Dirt
- Coarse Dirt: 100 crossings → Gravel
- Gravel: 200 crossings → Cobblestone or Mossy Cobblestone
- Established Cobblestone created by Living Paths: 1,000 crossings → Stone
- Stone originating from a Living Paths path: 1,000 crossings → Smooth Stone

These are configurable defaults intended to work without mandatory setup. Gameplay and long-term survival testing may still lead to balancing changes before `1.0.0`.
