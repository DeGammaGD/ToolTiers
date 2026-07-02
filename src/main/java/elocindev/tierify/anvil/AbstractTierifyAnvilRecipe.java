package elocindev.tierify.anvil;

import elocindev.tierify.tier.TierManager;
import elocindev.tierify.util.AnvilTierUpgradeHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

abstract class AbstractTierifyAnvilRecipe implements TierifyAnvilRecipe {

    protected static ItemStack prepareTieredResult(ItemStack baseStack, Identifier tierId) {
        ItemStack result = baseStack.copy();
        TierManager.removeTier(result);
        TierManager.setTier(result, tierId);
        TierManager.repairTier(result);
        TierManager.rebuildAttributes(result);
        return result;
    }

    protected static Identifier buildTierVariant(Identifier sourceTier, String quality) {
        return AnvilTierUpgradeHelper.buildTierVariant(sourceTier, quality);
    }

    protected static String getTierQuality(Identifier tierId) {
        return AnvilTierUpgradeHelper.getTierQuality(tierId);
    }

    protected static Identifier pickResultTier(ItemStack left, Identifier leftTier, Identifier rightTier) {
        return AnvilTierUpgradeHelper.pickResultTier(left.getItem(), leftTier, rightTier);
    }

    protected static boolean isStandardReforgeOperationValid(ItemStack left, ItemStack right) {
        return AnvilTierUpgradeHelper.isStandardReforgeOperationValid(left, right);
    }

    protected static boolean isTotemReforgeOperationValid(ItemStack left, ItemStack right) {
        return AnvilTierUpgradeHelper.isTotemReforgeOperationValid(left, right);
    }

    protected static boolean isNetherStarMythicReforgeOperationValid(ItemStack left, ItemStack right) {
        return AnvilTierUpgradeHelper.isNetherStarMythicReforgeOperationValid(left, right);
    }

    protected static boolean isEchoShardRerollOperationValid(ItemStack left, ItemStack right) {
        return AnvilTierUpgradeHelper.isEchoShardRerollOperationValid(left, right);
    }

    protected static Identifier getTier(ItemStack stack) {
        return TierManager.getTier(stack);
    }

    @Override
    public abstract boolean matches(ItemStack left, ItemStack right, Player player);

    @Override
    public abstract Optional<ItemStack> createResult(ItemStack left, ItemStack right, Player player);
}