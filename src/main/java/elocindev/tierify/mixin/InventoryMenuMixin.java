package elocindev.tierify.mixin;

import elocindev.tierify.util.CraftingTierHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public class InventoryMenuMixin {

    @Inject(method = "quickMoveStack", at = @At("HEAD"))
    private void tierifyShiftCraftOutput(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        CraftingTierHelper.applyTierToCraftingResult((AbstractContainerMenu) (Object) this, player, index);
    }
}
