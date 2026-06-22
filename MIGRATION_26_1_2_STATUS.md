# ToolTiers Migration Status: Minecraft 26.1.2 (Fabric, Mojmap)

## Current State

- Baseline stability was previously confirmed on Minecraft 1.21.1.
- Migration configuration was updated to target 26.1.2:
   - `minecraft_version=26.1.2`
   - `loader_version=0.19.3`
   - `fabric_version=0.152.1+26.1.2`
   - `cloth_config_version=26.1.154`
   - `mod_menu_version=20.0.0-beta.3`
   - Java toolchain switched to JDK 25 (required by 26.1.2)

## Active Blocker (Hard Stop Before Compilation)

Build currently fails during Minecraft setup with:

> Failed to setup Minecraft, java.lang.RuntimeException: Failed to find official mojang mappings for 26.1.2

Verified metadata status:

- Fabric game list includes `26.1.2` as stable.
- Yarn endpoint for 26.1.2 returns empty:
   - `https://meta.fabricmc.net/v2/versions/yarn/26.1.2` -> `[]`
- Loom in this workspace resolves to `1.17.12` from `1.17-SNAPSHOT` and still cannot resolve official mappings for `26.1.2`.

## Why Migration Cannot Continue Yet

The requested port path is Mojmap/Fabric compatibility updates. That requires resolvable named mappings for the target game version. For `26.1.2`, neither of the usable named mapping paths is currently available in this environment:

- Official Mojang mappings: not resolvable by Loom for `26.1.2`.
- Yarn mappings: not published for `26.1.2`.

Without one of those mapping sets, the project cannot reach Java compilation, so API/mixin migration steps cannot start.

## Unblock Conditions

Migration can proceed immediately when either condition is true:

1. Official mappings for `26.1.2` become resolvable via Loom.
2. Yarn mappings for `26.1.2` are published.

Once unblocked, next sequence remains:

1. `gradlew.bat clean build --no-daemon`
2. Fix compile errors using 26.1.2 API replacements only.
3. Fix mixin targets/descriptors without disabling core mixins.
4. `gradlew.bat runClient --no-daemon`
5. Validate startup, Mod Menu, world load, and unchanged behavior.
