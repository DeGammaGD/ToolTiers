package elocindev.tierify.anvil;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class NetherStarUpgradeRecipe extends AbstractTierifyAnvilRecipe {

    @Override
    public boolean matches(ItemStack left, ItemStack right, Player player) {
        return isNetherStarMythicReforgeOperationValid(left, right);
    }

    @Override
    public Optional<ItemStack> createResult(ItemStack left, ItemStack right, Player player) {
        if (!matches(left, right, player)) {
            return Optional.empty();
        }

        Identifier leftTier = getTier(left);
        if (leftTier == null) {
            return Optional.empty();
        }

        Identifier targetTier = buildTierVariant(leftTier, "mythic");
        if (targetTier == null) {
            return Optional.empty();
        }

        return Optional.of(prepareTieredResult(left, targetTier));
    }
}