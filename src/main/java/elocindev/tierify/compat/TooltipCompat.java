package elocindev.tierify.compat;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Compatibility boundary for tooltip/border integrations.
 */
public final class TooltipCompat {

    private TooltipCompat() {
    }

    public static void applyTierBorder(ItemStack stack, Identifier tierId) {
        String color = ItemBordersCompat.getColorForIdentifier(tierId);
        ItemBordersCompat.addBorder(stack, color, color);
    }
}
