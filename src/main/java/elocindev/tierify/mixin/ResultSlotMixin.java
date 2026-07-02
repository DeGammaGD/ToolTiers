package elocindev.tierify.mixin;

import elocindev.tierify.tier.TierManager;
import elocindev.tierify.Tierify;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public class ResultSlotMixin {

    @Shadow @Final private Player player;

    private void finalizeCraftedStack(Player player, ItemStack stack) {
        if (!Tierify.CONFIG.craftingModifier || stack.isEmpty() || player.level().isClientSide()) {
            return;
        }

        if (!(player instanceof ServerPlayer)) {
            return;
        }

        TierManager.applyTierIfNeeded(stack);
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void tierifyCraftResultOnTake(Player player, ItemStack stack, CallbackInfo info) {
        finalizeCraftedStack(player, stack);
    }

    @Inject(method = "onQuickCraft(Lnet/minecraft/world/item/ItemStack;I)V", at = @At("HEAD"), require = 0)
    private void tierifyCraftResultOnQuickCraftCount(ItemStack stack, int amount, CallbackInfo info) {
        finalizeCraftedStack(this.player, stack);
    }

    @Inject(method = "onQuickCraft(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), require = 0)
    private void tierifyCraftResultOnQuickCraftStack(ItemStack craftedStack, ItemStack originalStack, CallbackInfo info) {
        finalizeCraftedStack(this.player, craftedStack);
    }
}
