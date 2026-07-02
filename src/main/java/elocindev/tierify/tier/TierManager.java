package elocindev.tierify.tier;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"null"})
public final class TierManager {

    private TierManager() {
    }

    public static boolean hasTier(ItemStack stack) {
        return TierStorage.hasTier(stack);
    }

    public static boolean hasTierMarker(ItemStack stack) {
        return TierStorage.hasTierMarker(stack);
    }

    @Nullable
    public static Identifier getTier(ItemStack stack) {
        return TierStorage.getTierId(stack);
    }

    public static void applyTierIfNeeded(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        Identifier existingTier = getTier(stack);
        if (existingTier != null && elocindev.tierify.Tierify.ATTRIBUTE_DATA_LOADER.getItemAttributes().containsKey(existingTier)) {
            return;
        }

        Identifier generatedTier = TierSelector.selectRandomTierId(null, stack.getItem(), false);
        if (generatedTier == null) {
            return;
        }

        TierStorage.applyTierById(generatedTier, stack);
    }

    public static void applyTier(ItemStack stack, Identifier tierId) {
        TierStorage.applyTierById(tierId, stack);
    }

    public static void rerollTier(@Nullable Player playerEntity, ItemStack stack, boolean reforge) {
        TierStorage.assignOrRepairTier(playerEntity, stack, reforge);
    }

    public static int repairTier(ItemStack stack) {
        return TierStorage.applyTierAttributes(stack);
    }

    public static void removeTier(ItemStack stack) {
        TierStorage.removeTier(stack);
    }

    public static int rebuildAttributes(ItemStack stack) {
        return AttributeComponentBuilder.rebuildAttributeModifiersComponent(stack);
    }

    public static void setTier(ItemStack stack, Identifier tierId) {
        TierStorage.setTier(stack, tierId);
    }

    public static void assignTierWithRepair(@Nullable Player playerEntity, ItemStack stack, boolean reforge) {
        TierStorage.assignOrRepairTier(playerEntity, stack, reforge);
    }

    public static int countGeneratedModifiers(ItemStack stack) {
        return AttributeComponentBuilder.countGeneratedTierModifiers(stack);
    }
}
