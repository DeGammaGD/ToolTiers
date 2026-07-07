package elocindev.tierify.mixin;

import elocindev.tierify.anvil.TierifyAnvilRecipeManager;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
@SuppressWarnings({"null"})
public abstract class AnvilScreenHandlerMixin {

    @Shadow
    private DataSlot cost;

    @Shadow
    private int repairItemCountCost;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void tierify$allowTierUpgradeValidationPreview(CallbackInfo info) {
        AnvilMenu menu = (AnvilMenu) (Object) this;
        ItemStack left = menu.getSlot(0).getItem();
        ItemStack right = menu.getSlot(1).getItem();
        ItemStack preview = TierifyAnvilRecipeManager.createResult(left, right, null).orElse(null);
        if (preview == null) {
            return;
        }

        menu.getSlot(2).set(preview);
        this.cost.set(1);
        this.repairItemCountCost = 1;
        menu.broadcastChanges();
        info.cancel();
    }


    @Inject(method = "onTake", at = @At("HEAD"))
    private void tierify$applyAnvilTierUpgradeOnTake(Player player, ItemStack resultStack, CallbackInfo info) {
        AnvilMenu menu = (AnvilMenu) (Object) this;
        TierifyAnvilRecipeManager.syncRecipeResultIfNeeded(player, menu, resultStack);
    }
}
