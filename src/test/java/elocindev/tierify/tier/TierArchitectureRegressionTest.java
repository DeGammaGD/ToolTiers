package elocindev.tierify.tier;

import elocindev.tierify.anvil.TierifyAnvilRecipeManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TierArchitectureRegressionTest {

    @Test
    void tierSelectionDeterminismSortByWeight() throws Exception {
        List<Integer> weights = new ArrayList<>(List.of(10, 2, 7));
        List<String> values = new ArrayList<>(List.of("high", "low", "mid"));

        Method sortByWeight = TierSelector.class.getDeclaredMethod("sortByWeight", List.class, List.class);
        sortByWeight.setAccessible(true);
        sortByWeight.invoke(null, weights, values);

        assertEquals(List.of(2, 7, 10), weights);
        assertEquals(List.of("low", "mid", "high"), values);
    }

    @Test
    void mythicIsExcludedFromNormalRandomSelection() throws Exception {
        Method isMythicTier = TierSelector.class.getDeclaredMethod("isMythicTier", Identifier.class);
        isMythicTier.setAccessible(true);

        boolean mythic = (boolean) isMythicTier.invoke(null, Identifier.parse("tiered:melee_weapons/mythic"));
        boolean legendary = (boolean) isMythicTier.invoke(null, Identifier.parse("tiered:melee_weapons/legendary"));

        assertTrue(mythic);
        assertEquals(false, legendary);
    }

    @Test
    void legendarySameTierUpgradeCapsAtLegendary() throws Exception {
        Class<?> tierRankClass = Class.forName("elocindev.tierify.util.AnvilTierUpgradeHelper$TierRank");
        Method nextRank = elocindev.tierify.util.AnvilTierUpgradeHelper.class.getDeclaredMethod("nextRank", tierRankClass);
        nextRank.setAccessible(true);

        @SuppressWarnings({"unchecked", "rawtypes"})
        Enum<?> legendary = Enum.valueOf((Class) tierRankClass, "LEGENDARY");
        Object capped = nextRank.invoke(null, legendary);
        assertEquals(legendary, capped);
    }

    @Test
    void recipeManagerRegistersAllTierifyRecipes() {
        assertEquals(4, elocindev.tierify.anvil.TierifyAnvilRecipeManager.getRecipes().size());
        assertTrue(TierifyAnvilRecipeManager.getRecipes().stream().anyMatch(recipe -> recipe.getClass().getSimpleName().equals("StandardReforgeRecipe")));
        assertTrue(TierifyAnvilRecipeManager.getRecipes().stream().anyMatch(recipe -> recipe.getClass().getSimpleName().equals("TotemReforgeRecipe")));
        assertTrue(TierifyAnvilRecipeManager.getRecipes().stream().anyMatch(recipe -> recipe.getClass().getSimpleName().equals("NetherStarUpgradeRecipe")));
        assertTrue(TierifyAnvilRecipeManager.getRecipes().stream().anyMatch(recipe -> recipe.getClass().getSimpleName().equals("EchoShardRerollRecipe")));
    }

    @Test
    void modifierRebuildDeterminismDiminishingReturns() {
        Map<String, Integer> counters = new HashMap<>();

        double first = ModifierRollGenerator.applyMovementSpeedDiminishingReturns("minecraft:movement_speed", 1.0D, counters);
        double second = ModifierRollGenerator.applyMovementSpeedDiminishingReturns("minecraft:movement_speed", 1.0D, counters);
        double third = ModifierRollGenerator.applyMovementSpeedDiminishingReturns("minecraft:movement_speed", 1.0D, counters);

        assertEquals(1.0D, first);
        assertEquals(0.5D, second);
        assertEquals(0.25D, third);
    }

    @Test
    void tierApplicationIdempotenceGeneratedRollNbtRoundTrip() {
        CompoundTag root = new CompoundTag();
        List<GeneratedAttributeRoll> rolls = List.of(
                new GeneratedAttributeRoll("minecraft:attack_damage", "tiered:test_1", AttributeModifier.Operation.ADD_VALUE, 1.0D,
                        new EquipmentSlot[] {EquipmentSlot.MAINHAND}, null),
                new GeneratedAttributeRoll("minecraft:attack_speed", "tiered:test_2", AttributeModifier.Operation.ADD_MULTIPLIED_BASE, 0.1D,
                        null, new EquipmentSlot[] {EquipmentSlot.MAINHAND})
        );

        ModifierRollGenerator.writeGeneratedRollsToNbt(root, rolls);
        String snapshot1 = root.toString();

        List<GeneratedAttributeRoll> decoded = ModifierRollGenerator.readGeneratedRollsFromNbt(root);
        ModifierRollGenerator.writeGeneratedRollsToNbt(root, decoded);
        String snapshot2 = root.toString();

        assertEquals(snapshot1, snapshot2);
    }

    @Test
    void nbtBackwardCompatibilityMissingGeneratedRollData() {
        CompoundTag root = new CompoundTag();
        CompoundTag generated = new CompoundTag();
        generated.putInt("count", 1);
        generated.put("entry_0", new CompoundTag());
        root.put("TieredGeneratedAttributes", generated);

        List<GeneratedAttributeRoll> decoded = ModifierRollGenerator.readGeneratedRollsFromNbt(root);
        assertTrue(decoded.isEmpty());
    }

    @Test
    void storageMigrationSlotCsvParsingCompatibility() {
        CompoundTag root = new CompoundTag();
        CompoundTag generated = new CompoundTag();
        generated.putInt("count", 1);
        CompoundTag entry = new CompoundTag();
        entry.putString("type", "minecraft:attack_damage");
        entry.putString("modifier_id", "tiered:test_slot_csv");
        entry.putString("operation", AttributeModifier.Operation.ADD_VALUE.name());
        entry.putDouble("amount", 1.25D);
        entry.putString("required_slots", "MAINHAND,OFFHAND");
        entry.putString("optional_slots", "HEAD,INVALID");
        generated.put("entry_0", entry);
        root.put("TieredGeneratedAttributes", generated);

        List<GeneratedAttributeRoll> decoded = ModifierRollGenerator.readGeneratedRollsFromNbt(root);
        assertEquals(1, decoded.size());
        assertArrayEquals(new EquipmentSlot[] {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}, decoded.get(0).requiredSlots);
        assertArrayEquals(new EquipmentSlot[] {EquipmentSlot.HEAD}, decoded.get(0).optionalSlots);
    }

    @Test
    void attributeRebuildConsistencyMasteryMultiplier() {
        List<GeneratedAttributeRoll> rolls = List.of(
                new GeneratedAttributeRoll("tiered:generic.mastery", "tiered:mastery", AttributeModifier.Operation.ADD_VALUE, 0.2D,
                        new EquipmentSlot[] {EquipmentSlot.MAINHAND}, null),
                new GeneratedAttributeRoll("minecraft:attack_damage", "tiered:damage", AttributeModifier.Operation.ADD_VALUE, 1.0D,
                        new EquipmentSlot[] {EquipmentSlot.MAINHAND}, null),
                new GeneratedAttributeRoll("tiered:generic.mastery", "tiered:mastery_2", AttributeModifier.Operation.ADD_VALUE, 0.1D,
                        new EquipmentSlot[] {EquipmentSlot.OFFHAND}, null)
        );

        double mainhandMastery = ModifierRollGenerator.getMasteryMultiplierForSlot(rolls, EquipmentSlot.MAINHAND);
        double mainhandMasteryAgain = ModifierRollGenerator.getMasteryMultiplierForSlot(rolls, EquipmentSlot.MAINHAND);

        assertEquals(0.2D, mainhandMastery);
        assertEquals(mainhandMastery, mainhandMasteryAgain);
    }
}
