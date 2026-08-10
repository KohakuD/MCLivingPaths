# Living Paths – Design

## Core Idea

Living Paths changes the landscape based on how frequently certain blocks are walked over by players and, later, other entities.

Paths should emerge organically and should not look identical everywhere.

## Core Rules

### Standard Wear Progression

Grass Block
→ Dirt Path
→ Coarse Dirt
→ Gravel
→ Cobblestone

Regular Dirt is deliberately excluded as a wear stage because nearby Grass Blocks or Mycelium can spread back onto it.

### Protected Blocks

Farmland must never be modified by Living Paths.

### Podzol and Mycelium

Podzol and Mycelium can also wear down, but they are significantly more resistant than Grass Blocks.

For the first version, both should require roughly three times as much traffic as Grass Blocks. This is explicitly a test value and may be adjusted after gameplay testing.

### Biome-Dependent Development

Later versions should create different path characteristics depending on the biome.

Examples:

- Forest areas: Podzol and Rooted Dirt
- Damp or heavily vegetated areas: Moss and Mossy Cobblestone
- Open landscapes: Dirt Path, Coarse Dirt and Gravel

Biome-dependent development should not mean that every block follows the same fixed sequence. The goal is a natural mixture of more and less heavily worn surfaces.

### Highest Regular Wear Stage

Cobblestone is initially the highest regular wear stage.

Stone will not automatically become the next stage. A rare later development for extremely heavily used paths may be evaluated separately.

### Natural Appearance

In the long term, paths should not consist of uniform rows of identical blocks. Actual traffic should create a stronger central track, softer edges and different levels of wear.

Moss should be more likely to appear on lightly travelled edges than in the heavily used centre of a path.

## Technical Principles

- Processing happens server-side.
- A player does not generate wear every game tick; wear is counted only when they actually move onto a new ground block.
- Usage is tracked per block position.
- Wear data must be stored persistently.
- Farmland is completely excluded through a protected-block rule.
- The architecture should allow later support for vanilla mobs and MineColonies Citizens.

## Initial Test Thresholds for 0.1.0

The following values are only a starting point for gameplay testing:

- Grass Block: 25 crossings → Dirt Path
- Dirt Path: 50 additional crossings → Coarse Dirt
- Coarse Dirt: 100 additional crossings → Gravel
- Gravel: 200 additional crossings → Cobblestone

Podzol and Mycelium should use approximately a 3× resistance factor compared with Grass Blocks in 0.1.0.
