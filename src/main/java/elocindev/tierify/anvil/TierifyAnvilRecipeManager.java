package elocindev.tierify.anvil;

import elocindev.tierify.tier.TierManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class TierifyAnvilRecipeManager {

    private static final List<TierifyAnvilRecipe> RECIPES = new ArrayList<>();

    static {
        registerRecipe(new StandardReforgeRecipe());
        registerRecipe(new TotemReforgeRecipe());
        registerRecipe(new NetherStarUpgradeRecipe());
        registerRecipe(new EchoShardRerollRecipe());
    }

    private TierifyAnvilRecipeManager() {
    }

    public static List<TierifyAnvilRecipe> getRecipes() {
        return Collections.unmodifiableList(RECIPES);
    }

    public static void registerRecipe(TierifyAnvilRecipe recipe) {
        if (recipe != null) {
            RECIPES.add(recipe);
        }
    }

    public static Optional<ItemStack> createResult(ItemStack left, ItemStack right, Player player) {
        for (TierifyAnvilRecipe recipe : RECIPES) {
            if (!recipe.matches(left, right, player)) {
                continue;
            }

            Optional<ItemStack> result = recipe.createResult(left, right, player);
            if (result.isPresent()) {
                ItemStack preview = result.get().copy();
                // Preview intentionally hides generated tier attributes until pickup.
                preview.remove(DataComponents.ATTRIBUTE_MODIFIERS);
                return Optional.of(preview);
            }
        }

        return Optional.empty();
    }

    private static Optional<TierifyAnvilRecipe> findMatchingRecipe(ItemStack left, ItemStack right, Player player) {
        for (TierifyAnvilRecipe recipe : RECIPES) {
            if (recipe.matches(left, right, player)) {
                return Optional.of(recipe);
            }
        }

        return Optional.empty();
    }

    public static void syncRecipeResultIfNeeded(Player player, AnvilMenu menu, ItemStack resultStack) {
        ItemStack left = menu.getSlot(0).getItem();
        ItemStack right = menu.getSlot(1).getItem();
        if (left.isEmpty() || right.isEmpty() || resultStack.isEmpty()) {
            return;
        }

        if (findMatchingRecipe(left, right, player).isEmpty()) {
            return;
        }

        // Rebuild real generated attributes at pickup time using already generated rolls.
        TierManager.repairTier(resultStack);
        TierManager.rebuildAttributes(resultStack);

        menu.getSlot(2).set(resultStack);
        menu.getSlot(2).setChanged();
        player.getInventory().setChanged();
        menu.broadcastChanges();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
            serverPlayer.inventoryMenu.broadcastChanges();
        }
    }
}