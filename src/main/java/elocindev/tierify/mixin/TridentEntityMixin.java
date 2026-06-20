package elocindev.tierify.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.hit.EntityHitResult;

import elocindev.tierify.util.AttributeHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin extends PersistentProjectileEntity {

    public TridentEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void onEntityHitMixin(EntityHitResult hitResult, CallbackInfo info) {
        if (this.getOwner() instanceof ServerPlayerEntity) {
            this.setDamage(AttributeHelper.getExtraRangeDamage((PlayerEntity) this.getOwner(), (float) this.getDamage()));
        }
    }
}
