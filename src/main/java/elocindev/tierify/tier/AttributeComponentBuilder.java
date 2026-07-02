package elocindev.tierify.tier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import elocindev.tierify.Tierify;
import elocindev.tierify.api.AttributeTemplate;
import elocindev.tierify.api.PotentialAttribute;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({"null", "deprecation"})
public final class AttributeComponentBuilder {

    private static final String MINING_EFFICIENCY_ATTRIBUTE_ID = "tiered:generic.mining_efficiency";

    private AttributeComponentBuilder() {
    }

    private static boolean isValidAttributeTypeId(String attributeTypeId) {
        if (attributeTypeId == null || attributeTypeId.isBlank()) {
            return false;
        }

        try {
            return BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(attributeTypeId)).isPresent();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean slotArrayContains(EquipmentSlot[] slots, EquipmentSlot slot) {
        if (slots == null) return false;
        for (EquipmentSlot s : slots) {
            if (s == slot) return true;
        }
        return false;
    }

    private static boolean templateAppliesToSlot(AttributeTemplate template, EquipmentSlot slot) {
        return slotArrayContains(template.getRequiredEquipmentSlots(), slot)
                || slotArrayContains(template.getOptionalEquipmentSlots(), slot);
    }

    public static Multimap<Holder<Attribute>, AttributeModifier> buildTierAttributeMap(ItemStack itemStack, EquipmentSlot slot) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
        Identifier tierId = TierStorage.getTierId(itemStack);
        if (tierId == null) {
            return modifiers;
        }

        CompoundTag root = TierStorage.getCustomData(itemStack);
        java.util.List<GeneratedAttributeRoll> generatedRolls = ModifierRollGenerator.readGeneratedRollsFromNbt(root);
        Map<String, Integer> diminishingCounters = new HashMap<>();
        if (!generatedRolls.isEmpty()) {
            double masteryMultiplier = ModifierRollGenerator.getMasteryMultiplierForSlot(generatedRolls, slot);

            for (GeneratedAttributeRoll roll : generatedRolls) {
                if (roll == null) {
                    continue;
                }

                if (!ModifierRollGenerator.rollAppliesToSlot(roll, slot)) {
                    continue;
                }

                if (!isValidAttributeTypeId(roll.attributeTypeId)) {
                    continue;
                }

                Identifier baseModifierId;
                try {
                    baseModifierId = Identifier.parse(roll.modifierId);
                } catch (Exception ignored) {
                    continue;
                }

                Identifier modifierId = Identifier.fromNamespaceAndPath(baseModifierId.getNamespace(), baseModifierId.getPath() + "_" + slot.getName());
                double amount = roll.amount;
                if (masteryMultiplier != 0.0D && !ModifierRollGenerator.isMasteryAttributeType(roll.attributeTypeId)) {
                    amount = amount + masteryMultiplier;
                }
                amount = ModifierRollGenerator.applyMovementSpeedDiminishingReturns(roll.attributeTypeId, amount, diminishingCounters);
                AttributeModifier cloneModifier = new AttributeModifier(modifierId, amount, roll.operation);
                var key = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(roll.attributeTypeId));
                if (key.isEmpty()) {
                    continue;
                }

                modifiers.put(key.get(), cloneModifier);
            }

            return modifiers;
        }

        PotentialAttribute potentialAttribute = Tierify.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tierId);
        if (potentialAttribute == null) {
            return modifiers;
        }

        for (AttributeTemplate template : potentialAttribute.getAttributes()) {
            if (template == null || template.getEntityAttributeModifier() == null) {
                continue;
            }

            boolean applies = templateAppliesToSlot(template, slot);
            if (!applies) {
                continue;
            }

            if (!isValidAttributeTypeId(template.getAttributeTypeID())) {
                continue;
            }

            AttributeModifier baseModifier = template.getEntityAttributeModifier();
            Identifier baseModifierId = baseModifier.id();
            Identifier modifierId = Identifier.fromNamespaceAndPath(baseModifierId.getNamespace(), baseModifierId.getPath() + "_" + slot.getName());
            double amount = ModifierRollGenerator.applyMovementSpeedDiminishingReturns(template.getAttributeTypeID(), baseModifier.amount(), diminishingCounters);
            AttributeModifier cloneModifier = new AttributeModifier(modifierId, amount, baseModifier.operation());

            var key = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(template.getAttributeTypeID()));
            if (key.isPresent()) {
                modifiers.put(key.get(), cloneModifier);
            }
        }

        return modifiers;
    }

    public static int rebuildAttributeModifiersComponent(ItemStack itemStack) {
        ItemAttributeModifiers baseComponent = new ItemStack(itemStack.getItem().builtInRegistryHolder(), itemStack.getCount())
                .getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        Map<EquipmentSlotGroup, Double> miningEfficiencyTotals = new HashMap<>();

        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        EquipmentSlot armorSlot = null;
        if (equippable != null) {
            EquipmentSlot slot = equippable.slot();
            if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {
                armorSlot = slot;
            }
        }

        for (ItemAttributeModifiers.Entry entry : baseComponent.modifiers()) {
            EquipmentSlotGroup slotGroup = entry.slot();
            if (armorSlot != null && (slotGroup == EquipmentSlotGroup.ARMOR || slotGroup == EquipmentSlotGroup.BODY)) {
                slotGroup = EquipmentSlotGroup.bySlot(armorSlot);
            }

            Identifier attributeId = BuiltInRegistries.ATTRIBUTE.getKey(entry.attribute().value());
            if (attributeId != null && MINING_EFFICIENCY_ATTRIBUTE_ID.equals(attributeId.toString())) {
                miningEfficiencyTotals.merge(slotGroup, entry.modifier().amount(), Double::sum);
                continue;
            }

            builder.add(entry.attribute(), entry.modifier(), slotGroup);
        }

        int appliedModifierCount = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!Tierify.isPreferredEquipmentSlot(itemStack, slot)) {
                continue;
            }
            Multimap<Holder<Attribute>, AttributeModifier> generated = buildTierAttributeMap(itemStack, slot);
            double miningEfficiencyTotal = 0.0D;
            for (Map.Entry<Holder<Attribute>, AttributeModifier> entry : generated.entries()) {
                Identifier attributeId = BuiltInRegistries.ATTRIBUTE.getKey(entry.getKey().value());
                if (attributeId != null && MINING_EFFICIENCY_ATTRIBUTE_ID.equals(attributeId.toString())) {
                    miningEfficiencyTotal += entry.getValue().amount();
                    continue;
                }
                builder.add(entry.getKey(), entry.getValue(), EquipmentSlotGroup.bySlot(slot));
                appliedModifierCount++;
            }

            if (miningEfficiencyTotal != 0.0D) {
                EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(slot);
                miningEfficiencyTotals.merge(slotGroup, miningEfficiencyTotal, Double::sum);
            }
        }

        Holder<Attribute> miningEfficiency = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(MINING_EFFICIENCY_ATTRIBUTE_ID)).orElse(null);
        if (miningEfficiency != null) {
            for (Map.Entry<EquipmentSlotGroup, Double> entry : miningEfficiencyTotals.entrySet()) {
                double total = entry.getValue();
                if (total == 0.0D) {
                    continue;
                }

                builder.add(
                        miningEfficiency,
                        new AttributeModifier(
                                Tierify.id("mining_efficiency_" + entry.getKey().getSerializedName().toLowerCase(Locale.ROOT)),
                                total,
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        entry.getKey());
                appliedModifierCount++;
            }
        }

        ItemAttributeModifiers rebuilt = builder.build();
        itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, rebuilt);
        return appliedModifierCount;
    }

    public static int countGeneratedTierModifiers(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return 0;
        }

        int generatedModifierCount = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!Tierify.isPreferredEquipmentSlot(itemStack, slot)) {
                continue;
            }
            generatedModifierCount += buildTierAttributeMap(itemStack, slot).size();
        }
        return generatedModifierCount;
    }
}
