# Living Paths

Living Paths is a NeoForge mod for Minecraft 1.21.1 that lets frequently travelled terrain evolve into natural-looking paths over time.

The goal is to make movement leave visible, persistent traces in the world without turning the mod into an automatic road builder.

## Current status

Living Paths is currently in early development (`0.1.0`).

The first version focuses on player-created wear:

- track actual movement across ground blocks
- evolve frequently travelled grass into progressively stronger path materials
- support Podzol and Mycelium with greater wear resistance
- never modify Farmland
- store wear data persistently
- process path wear server-side and remain multiplayer-friendly

Initial wear chain:

`Grass Block -> Dirt Path -> Coarse Dirt -> Gravel -> Cobblestone`

Future versions are planned to add biome-aware path variants, moss and rooted dirt, more organic path edges, vanilla entities and MineColonies citizens.

## Documentation

- [Design](docs/Design.md)
- [Roadmap](docs/Roadmap.md)
- [Deutsches Design](docs/de/Design.md)
- [Deutsche Roadmap](docs/de/Roadmap.md)

## Development

- Minecraft: `1.21.1`
- NeoForge: `21.1.x`
- Java: `21`

Build the project with:

```powershell
.\gradlew.bat build
```

Run the development client with:

```powershell
.\gradlew.bat runClient
```

## Languages

English is the primary project and mod language. German (`de_de`) is maintained alongside English from the beginning.

## License

All Rights Reserved.
