package elocindev.tierify.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;

@Mixin(ArmorItem.class)
public class ArmorItemMixin {

    @Shadow
    @Final
    protected RegistryEntry<ArmorMaterial> material;

    @Shadow
    @Final
    protected ArmorItem.Type type;

    private static final Identifier KNOCKBACK_RESISTANCE_ID = Identifier.of("tiered", "armor_knockback_resistance");

    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
    private void getAttributeModifiersMixin(CallbackInfoReturnable<AttributeModifiersComponent> info) {
        if (this.material != ArmorMaterials.NETHERITE && this.material.value().knockbackResistance() > 0.0001f) {
            AttributeModifiersComponent modifiers = info.getReturnValue();
            boolean hasTieredKnockback = modifiers.modifiers().stream()
                    .anyMatch(entry -> entry.modifier().id().equals(KNOCKBACK_RESISTANCE_ID));

            if (!hasTieredKnockback) {
                info.setReturnValue(modifiers.with(
                        EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                        new EntityAttributeModifier(KNOCKBACK_RESISTANCE_ID, this.material.value().knockbackResistance(), EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.forEquipmentSlot(this.type.getEquipmentSlot())
                ));
            }
        }
    }
}
