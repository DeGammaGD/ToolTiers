package elocindev.tierify.util;

import elocindev.tierify.tier.TierManager;
import elocindev.tierify.Tierify;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Shared entry point for assigning a tier to a crafting-result slot when a stack is shift-clicked out of a
 * crafting grid. Used by both {@code CraftingMenu} and {@code InventoryMenu} shift-transfer hooks so the tier
 * assignment path stays single-sourced through {@link ModifierUtils#applyTierIfNeeded(ItemStack)}.
 */
public final class CraftingTierHelper {

    private CraftingTierHelper() {
    }

    public static void applyTierToCraftingResult(AbstractContainerMenu menu, Player player, int index) {
        if (index != 0 || player.level().isClientSide() || !Tierify.CONFIG.craftingModifier) {
            return;
        }

        Slot resultSlot = menu.getSlot(0);
        if (!resultSlot.hasItem()) {
            return;
        }

        ItemStack stack = resultSlot.getItem();
        if (stack.isEmpty()) {
            return;
        }

        TierManager.applyTierIfNeeded(stack);
    }
}
