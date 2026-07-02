package elocindev.tierify.anvil;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class EchoShardRerollRecipe extends AbstractTierifyAnvilRecipe {

    @Override
    public boolean matches(ItemStack left, ItemStack right, Player player) {
        return isEchoShardRerollOperationValid(left, right);
    }

    @Override
    public Optional<ItemStack> createResult(ItemStack left, ItemStack right, Player player) {
        if (!matches(left, right, player)) {
            return Optional.empty();
        }

        var tierId = getTier(left);
        if (tierId == null) {
            return Optional.empty();
        }

        return Optional.of(prepareTieredResult(left, tierId));
    }
}