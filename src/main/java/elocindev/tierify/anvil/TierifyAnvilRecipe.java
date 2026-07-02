package elocindev.tierify.anvil;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface TierifyAnvilRecipe {

    boolean matches(ItemStack left, ItemStack right, Player player);

    Optional<ItemStack> createResult(ItemStack left, ItemStack right, Player player);
}