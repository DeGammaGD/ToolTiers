package elocindev.tierify.mixin;

import elocindev.tierify.util.AnvilTierUpgradeHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCombinerMenu.class)
public class ItemCombinerMenuMixin {

    @Inject(method = "quickMoveStack", at = @At("HEAD"))
    private void tierify$finalizeAnvilReforgeBeforeShiftTransfer(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index != 2) {
            return;
        }

        if (!((Object) this instanceof AnvilMenu menu)) {
            return;
        }

        ItemStack resultStack = menu.getSlot(2).getItem();
        AnvilTierUpgradeHelper.applyTierUpgradeResultIfNeeded(player, menu, resultStack);
    }
}