package elocindev.tierify.tier;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

final class GeneratedAttributeRoll {
    final String attributeTypeId;
    final String modifierId;
    final AttributeModifier.Operation operation;
    final double amount;
    final EquipmentSlot[] requiredSlots;
    final EquipmentSlot[] optionalSlots;

    GeneratedAttributeRoll(String attributeTypeId,
                           String modifierId,
                           AttributeModifier.Operation operation,
                           double amount,
                           EquipmentSlot[] requiredSlots,
                           EquipmentSlot[] optionalSlots) {
        this.attributeTypeId = attributeTypeId;
        this.modifierId = modifierId;
        this.operation = operation;
        this.amount = amount;
        this.requiredSlots = requiredSlots;
        this.optionalSlots = optionalSlots;
    }
}
