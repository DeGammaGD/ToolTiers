package elocindev.tierify.mixin;

import draylar.tiered.api.ModifierUtils;
import elocindev.tierify.util.AnvilTierUpgradeHelper;
import net.minecraft.resources.Identifier;
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

    @Inject(method = "createResult", at = @At("TAIL"))
    private void tierify$allowTierUpgradeValidationPreview(CallbackInfo info) {
        AnvilMenu menu = (AnvilMenu) (Object) this;
        ItemStack left = menu.getSlot(0).getItem();
        ItemStack right = menu.getSlot(1).getItem();
        ItemStack vanillaResult = menu.getSlot(2).getItem();
        if (!AnvilTierUpgradeHelper.isTierUpgradeOperationValid(left, right)) {
            return;
        }

        Identifier leftTier = ModifierUtils.getAttributeID(left);
        Identifier rightTier = ModifierUtils.getAttributeID(right);
        if (leftTier == null || rightTier == null) {
            return;
        }

        Identifier targetTier = AnvilTierUpgradeHelper.pickResultTier(left.getItem(), leftTier, rightTier);
        if (targetTier == null) {
            return;
        }

        ItemStack preview = vanillaResult.isEmpty() ? left.copy() : vanillaResult.copy();
        ModifierUtils.removeItemStackAttribute(preview);
        ModifierUtils.setTier(preview, targetTier);
        menu.getSlot(2).set(preview);

        if (vanillaResult.isEmpty()) {
            this.cost.set(1);
        }
        menu.broadcastChanges();
    }


    @Inject(method = "onTake", at = @At("HEAD"))
    private void tierify$applyAnvilTierUpgradeOnTake(Player player, ItemStack resultStack, CallbackInfo info) {
        AnvilMenu menu = (AnvilMenu) (Object) this;
        AnvilTierUpgradeHelper.applyTierUpgradeResultIfNeeded(player, menu, resultStack);
    }
}
