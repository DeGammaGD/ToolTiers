package elocindev.tierify.util;

import draylar.tiered.api.CustomEntityAttributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class AttributeHelper {

    private static float sumAttributeModifiers(Player playerEntity, net.minecraft.world.entity.ai.attributes.Attribute[] attributes) {
        float total = 0.0f;
        for (var attribute : attributes) {
            AttributeInstance instance = playerEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
            if (instance == null) {
                continue;
            }

            for (AttributeModifier modifier : instance.getModifiers()) {
                total += (float) modifier.amount();
            }
        }
        return total;
    }

    public static boolean shouldMeeleCrit(Player playerEntity) {
        float critChance = sumAttributeModifiers(playerEntity, CustomEntityAttributes.CRITICAL_CHANCE_ATTRIBUTES);
        if (critChance != 0.0f) {
            return playerEntity.getRandom().nextDouble() < critChance;
        }
        return false;
    }

    public static float getExtraDigSpeed(Player playerEntity, float oldDigSpeed) {
        float extraDigSpeed = oldDigSpeed;
        boolean foundModifier = false;
        for (var attribute : CustomEntityAttributes.HASTE_ATTRIBUTES) {
            AttributeInstance instance = playerEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
            if (instance == null) {
                continue;
            }

            for (AttributeModifier modifier : instance.getModifiers()) {
                foundModifier = true;
                float amount = (float) modifier.amount();

                if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    extraDigSpeed += amount;
                } else {
                    extraDigSpeed *= (amount + 1);
                }
            }
        }

        if (foundModifier) {
            return extraDigSpeed;
        }

        return oldDigSpeed;
    }

    public static float getExtraRangeDamage(Player playerEntity, float oldDamage) {
        float rangeDamage = oldDamage;
        boolean foundModifier = false;
        for (var attribute : CustomEntityAttributes.RANGED_ATTACK_DAMAGE_ATTRIBUTES) {
            AttributeInstance instance = playerEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
            if (instance == null) {
                continue;
            }

            for (AttributeModifier modifier : instance.getModifiers()) {
                foundModifier = true;
                float amount = (float) modifier.amount();

                if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    rangeDamage += amount;
                } else {
                    rangeDamage *= (amount + 1.0f);
                }
            }
        }

        if (foundModifier) {
            return Math.min(rangeDamage, Integer.MAX_VALUE);
        }
        return oldDamage;
    }

    public static float getExtraCritDamage(Player playerEntity, float oldDamage) {
        float customChance = sumAttributeModifiers(playerEntity, CustomEntityAttributes.CRITICAL_CHANCE_ATTRIBUTES);
        if (customChance != 0.0f) {
            if (playerEntity.level().getRandom().nextFloat() > (1.0f - Math.abs(customChance))) {
                float extraCrit = oldDamage;
                if (customChance < 0.0f) {
                    extraCrit = extraCrit / 2.0f;
                }
                return oldDamage + Math.min(customChance > 0.0f ? extraCrit : -extraCrit, Integer.MAX_VALUE);
            }
        }
        return oldDamage;
    }

}
