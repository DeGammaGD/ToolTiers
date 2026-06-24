# ToolTiers

ToolTiers is a Minecraft 26.1.2 Fabric port and continuation of the **Tiered** concept, with roots in the community-driven **Tierify** project. This mod adds randomized quality tiers to tools, weapons, and armor with attribute-based progression and customization.

ToolTiers continues the development of tier-based item progression with modern Minecraft version support (26.1.2) and an improved data-driven architecture.

The original mod, **Tiered**, is inspired by [Quality Tools](https://www.curseforge.com/minecraft/mc-mods/quality-tools).

<img src="resources/legendary_chestplate.png" width="400">

## Features

### Currently Functional

- **Fabric Port for Minecraft 26.1.2**: Active Fabric-based continuation of Tiered/TieredZ concepts
- **Tier Generation System**: Weighted tier selection using verifier rules from datapack definitions
- **Tiered Item Sources**: Tiered items are produced through normal gameplay flows including crafting, loot, and world-driven acquisition paths
- **Multiple Attribute System**: Tiered items can roll multiple generated attributes from selected pools
- **Category-Based Modifier Pools**: Attribute options are defined per category and tier through datapack pools
- **Custom Tier Attributes**: Supports vanilla and custom attribute entries via JSON
- **Vanilla Anvil Reforging Behavior**:
  - Combining compatible item types is supported
  - Same item type with the same tier can upgrade to the next tier
  - Different tier combinations follow current higher-tier result rules
  - Vanilla enchantment behavior is preserved
- **Legacy Item Compatibility**: Older tier ids are mapped through compatibility aliases so existing items remain valid
- **Tooltip Styling**: Tier tooltip borders and colors remain fully supported

### In Development

- **Expanded Attribute System**: Ongoing extension of attribute pools and specialization by category
- **Additional Attributes and Balancing**: Continuous tuning of weights, ranges, and tier outcomes
- **More Item Category Support**: Broader coverage for additional modded and vanilla-adjacent categories
- **Gameplay/Balance Iteration**: Ongoing balancing and progression improvements
- **Reforge Polishing**: Quality and ruleset refinement around current anvil-based reforging flow

## 26.1.2 Port

- Updated from older Minecraft versions
- Migrated mappings and API changes (Mojmap)
- Removed outdated dependencies
- Stabilized Fabric runtime
- Modernized item registration and mixins

## Installation

ToolTiers requires:

- [Fabric Loader](https://fabricmc.net/)
- [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
- [Cloth Config](https://www.curseforge.com/minecraft/mc-mods/cloth-config)
- [Mod Menu](https://www.curseforge.com/minecraft/mc-mods/modmenu) (optional)

## Customization

ToolTiers uses a **datapack-driven system** for tier selection and attribute generation. Core behavior is controlled through JSON resources and can be customized without Java changes.

### System Architecture

- **Internal Namespace**: Uses mod id `tiered` for data and compatibility
- **Item Attributes**: Stored in `data/tiered/item_attributes/`
- **Modifier Pools**: Stored in `data/tiered/modifier_pools/`
- **Verifier Rules**: `item_attributes` entries define item/category compatibility and eligibility
- **Tier Availability/Weight**: `item_attributes` entries define tier selection weights
- **Pool References**: `item_attributes` entries reference the modifier pool used for generated rolls
- **Generated Attributes**: Actual rolled modifiers come from referenced `modifier_pools`

In short:

- `item_attributes` defines **what can roll** for an item and **how likely** each tier is
- `modifier_pools` defines **which attributes can be generated** once a tier is selected

You can create or extend custom tier behavior by adding new JSON entries in these datapack folders.

#### Current Flow

Tier selection:

- `item_attributes` -> verifier check -> weighted tier selection

Attribute generation:

- selected tier -> modifier pool -> attribute roll -> generated attributes stored on item

Generated attributes are rolled once per item and persisted. Reloads reuse stored generated data, and compatibility handling exists for older tiered items.

### Attributes

ToolTiers currently supports a category-aware generated attribute model.

Current focus attributes include:

- **Tools**:
  - Durability (`tiered:generic.durable`)
  - Dig Speed (`generic.dig_speed`, subject to future naming cleanup)
- **Armor**:
  - Armor (`generic.armor`)
  - Armor Toughness (`generic.armor_toughness`)
  - Durability (`tiered:generic.durable`)
  - Movement Speed (`generic.movement_speed`)
  - Max Health (`generic.max_health`)
- **Weapons**:
  - Critical Chance (`tiered:generic.crit_chance`)
  - Attack-related attributes (for example attack damage and ranged attack modifiers)

The attribute system is actively being expanded with additional specialized attributes and ongoing balance passes.

### Verifiers

ToolTiers uses a custom verifier system to determine whether items are eligible for specific tier attributes.

**Item ID Example:**

```json
"id": "minecraft:apple"
```

**Tag Example (Convention Tags):**

```json
"tag": "c:helmets"
```

**Fallback Matching:**

ToolTiers includes intelligent fallback matching for common item patterns (e.g., items ending with `_pickaxe`, `_sword`, `_spear`) to ensure comprehensive coverage.


### Weight

Weight determines how common a tier is. Higher weight means a higher chance to be applied.

### NBT

Tier data and generated modifier data are stored directly on each tiered item.

Stored item-side data includes:

- Assigned tier id
- Generated attribute roll data used to rebuild modifiers consistently
- Compatibility-aware handling for older tier/item data formats

This allows generated attributes to remain stable across reloads and inventory updates.

### Tooltip

Custom tooltip borders can be defined via resource packs.

- Border textures go in `assets/tiered/textures/gui`.
- Tooltip json files go in `assets/tiered/tooltips`.
- `background_gradient` can be configured.
- For color alpha format reference, see [this guide](https://gist.github.com/lopspower/03fb1cc0ac9f32ef38f4).
- See default tooltip data in `src/main/resources/assets/tiered/tooltips`.

Example:

```json
{
  "tooltips": [
    {
      "index": 0,
      "start_border_gradient": "FFBABABA",
      "end_border_gradient": "FF565656",
      "texture": "tiered_borders",
      "decider": [
        "set_the_id_here",
        "tiered:common_armor"
      ]
    }
  ]
}
```

### Reforge

ToolTiers currently uses **vanilla anvil-based reforging behavior**.

Current rules:

- Two compatible items can be combined in an anvil flow
- Same item type with the same tier can upgrade toward the next tier
- Combining different tiers follows the current tier result rules
- Vanilla anvil enchantment behavior is preserved

## Status

**Version:** 1.2.0  
**Minecraft:** 26.1.2  
**Loader:** Fabric  
**Mappings:** Mojmap  
**State:** Work in Progress

### Current State

- ToolTiers is an active Minecraft 26.1.2 port and continuation of Tiered/TieredZ.
- Core gameplay systems are functional in current builds.
- Tier generation, assignment, and attribute application are live.
- Ongoing work is focused on balancing, expanded attribute coverage, and continued iteration.
- Development is active and continuing.

## Credits

ToolTiers is built on the work of dedicated community developers:

- **Draylar1** - Original creator of [Tiered](https://github.com/Draylar/tiered), the foundational mod that inspired this project
- **Globox_Z** - Creator of [TieredZ](https://github.com/Globox1997/TieredZ), an early fork continuing Tiered development
- **Ameisin** - Creator of [Tierify_1.21.1](https://github.com/Ameisin/Tierify_1.21.1), a community port that maintained the mod during version transitions
- **nvb-uy** - Community maintainer of Tierify, bridging from TieredZ to modern versions

ToolTiers continues this legacy with **Minecraft 26.1.2 support** and focuses on maintaining a clean, data-driven architecture for future extensibility.

## License

ToolTiers source code in this repository is licensed under MIT.

Original Tiered and TieredZ creators retain authorship credit for their upstream work. ToolTiers continuation, migration, and maintenance contributions are by DeGammaGD.

Non-code assets may be All Rights Reserved where explicitly stated.
