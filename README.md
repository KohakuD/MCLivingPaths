# Living Paths

Living Paths is a NeoForge mod for Minecraft 1.21.1 that lets frequently travelled terrain evolve into natural-looking paths over time.

The goal is to make movement leave visible, persistent traces in the world without turning the mod into an automatic road builder.

## Current status

Living Paths is currently in early development. The latest version is `0.6.0`.

The current version supports:

- player-created wear from actual ground-block crossings
- biome-aware forest, damp and open-landscape path profiles
- organic path shoulders with stable, position-based variation
- selected vanilla mob traffic and optional animal traffic
- optional MineColonies Citizen traffic without a hard dependency
- configurable Player Two companion traffic without a hard dependency
- configurable thresholds, wear decay, protected blocks and entity weights
- slow regeneration of inactive paths created by Living Paths
- rare development of established Cobblestone into Stone and Smooth Stone
- persistent, multiplayer-friendly server-side processing
- an in-game configuration screen with English and German localisation

The standard open-landscape wear chain is:

`Grass Block -> Dirt Path -> Coarse Dirt -> Gravel -> Cobblestone -> Stone -> Smooth Stone`

Biome profiles can introduce Podzol, Rooted Dirt, Moss, Mud, Packed Mud and Mossy Cobblestone. Farmland is protected by default, and server administrators can configure the protected-block list.

The remaining work towards `1.0.0` focuses on long-term survival testing, real multiplayer validation, modpack compatibility, final performance and save-size review, localisation review and publication-ready packaging.

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

Operators can create a compact screenshot scene in a test world with:

```text
/livingpaths debug showcase
```

The command builds the complete standard progression four blocks in front of the player. It replaces a 28-by-7-block strip of terrain and clears two blocks of headroom, so it should only be used in a disposable or backed-up area.

## Languages

English is the primary project and mod language. German (`de_de`) is maintained alongside English from the beginning.

## License

Living Paths uses separate licenses for different parts of the project:

- Source code, build scripts, configuration, and original documentation text are licensed under the [MIT License](LICENSE-CODE).
- Original Living Paths textures, logos, icons, and other visual assets are licensed under [CC BY 4.0](LICENSE-ASSETS.md).

See [LICENSE](LICENSE) for the complete scope and exclusions.
