package elocindev.tierify.anvil;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class TotemReforgeRecipe extends AbstractTierifyAnvilRecipe {

    @Override
    public boolean matches(ItemStack left, ItemStack right, Player player) {
        return isTotemReforgeOperationValid(left, right);
    }

    @Override
    public Optional<ItemStack> createResult(ItemStack left, ItemStack right, Player player) {
        if (!matches(left, right, player)) {
            return Optional.empty();
        }

        var leftTier = getTier(left);
        var rightTier = getTier(right);
        if (leftTier == null || rightTier == null) {
            return Optional.empty();
        }

        String quality = getTierQuality(rightTier);
        Identifier targetTier = buildTierVariant(leftTier, quality);
        if (targetTier == null) {
            return Optional.empty();
        }

        return Optional.of(prepareTieredResult(left, targetTier));
    }
}