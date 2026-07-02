package elocindev.tierify.tier;

import elocindev.tierify.Tierify;
import elocindev.tierify.api.AttributeTemplate;
import elocindev.tierify.api.CustomEntityAttributes;
import elocindev.tierify.api.PotentialAttribute;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"null"})
public final class TierStorage {

    private TierStorage() {
    }

    public static boolean hasGeneratedAttributes(@Nullable Identifier tierId) {
        if (tierId == null) {
            return false;
        }

        PotentialAttribute assignedAttribute = Tierify.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tierId);
        return assignedAttribute != null && assignedAttribute.getAttributes() != null && !assignedAttribute.getAttributes().isEmpty();
    }

    static CompoundTag getCustomData(ItemStack stack) {
        CustomData component = stack.get(DataComponents.CUSTOM_DATA);
        return component != null ? component.copyTag() : new CompoundTag();
    }

    static void setCustomData(ItemStack stack, CompoundTag compound) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
    }

    public static boolean hasTier(ItemStack stack) {
        CustomData component = stack.get(DataComponents.CUSTOM_DATA);
        return component != null && component.copyTag().contains(Tierify.NBT_SUBTAG_KEY);
    }

    public static boolean hasTierMarker(ItemStack stack) {
        CustomData component = stack.get(DataComponents.CUSTOM_DATA);
        if (component == null) return false;
        CompoundTag tag = component.copyTag();
        return tag.contains(Tierify.NBT_SUBTAG_MARKER_KEY)
                && tag.getBoolean(Tierify.NBT_SUBTAG_MARKER_KEY).orElse(false);
    }

    @Nullable
    public static Identifier getTierId(ItemStack itemStack) {
        CompoundTag root = getCustomData(itemStack);
        if (root.contains(Tierify.NBT_SUBTAG_KEY)) {
            CompoundTag tiered = root.getCompound(Tierify.NBT_SUBTAG_KEY).orElse(new CompoundTag());
            return Identifier.parse(tiered.getString(Tierify.NBT_SUBTAG_DATA_KEY).orElse(""));
        }
        return null;
    }

    private static void clearTierNbtKeys(CompoundTag root, @Nullable Identifier tierId, boolean clearGeneratedRolls) {
        if (clearGeneratedRolls) {
            root.remove(ModifierRollGenerator.GENERATED_ATTRIBUTES_KEY);
        }
        if (tierId == null) {
            return;
        }

        PotentialAttribute previous = Tierify.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tierId);
        if (previous == null) {
            return;
        }

        HashMap<String, Object> nbtMap = previous.getNbtValues();
        if (nbtMap != null) {
            for (String key : nbtMap.keySet()) {
                if (!"Damage".equals(key)) {
                    root.remove(key);
                }
            }
        }

        for (AttributeTemplate template : previous.getAttributes()) {
            if (template == null) {
                continue;
            }
            if (CustomEntityAttributes.isDurabilityAttributeId(template.getAttributeTypeID())) {
                root.remove("durable");
                break;
            }
        }
    }

    private static void applyTierNbtValues(CompoundTag root,
                                           PotentialAttribute assignedAttribute,
                                           Identifier tierId,
                                           List<GeneratedAttributeRoll> generatedRolls) {
        HashMap<String, Object> nbtMap = assignedAttribute.getNbtValues();

        double durableAmount = ModifierRollGenerator.resolveDurableAmount(generatedRolls, assignedAttribute);
        if (Math.abs(durableAmount) > 0.0000001D) {
            if (nbtMap == null) {
                nbtMap = new HashMap<>();
            }
            nbtMap.put("durable", durableAmount);
        }

        if (nbtMap == null) {
            return;
        }

        for (HashMap.Entry<String, Object> entry : nbtMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                root.putString(key, (String) value);
            } else if (value instanceof Boolean) {
                root.putBoolean(key, (boolean) value);
            } else if (value instanceof Double) {
                if (Math.abs((double) value) % 1.0 < 0.0001D) {
                    root.putInt(key, (int) Math.round((double) value));
                } else {
                    root.putDouble(key, Math.round((double) value * 100.0) / 100.0);
                }
            }
        }
    }

    public static void setTier(ItemStack stack, Identifier tierId) {
        if (stack == null || stack.isEmpty() || tierId == null) {
            return;
        }

        CompoundTag root = getCustomData(stack);
        Identifier previousTier = getTierId(stack);
        clearTierNbtKeys(root, previousTier, true);

        CompoundTag tiered = new CompoundTag();
        tiered.putString(Tierify.NBT_SUBTAG_DATA_KEY, tierId.toString());
        root.put(Tierify.NBT_SUBTAG_KEY, tiered);
        root.putBoolean(Tierify.NBT_SUBTAG_MARKER_KEY, true);

        CompoundTag colors = new CompoundTag();
        String tierColor = TierColorResolver.getColorForTier(tierId);
        if (tierColor != null) {
            colors.putString("top", tierColor);
            colors.putString("bottom", tierColor);
            root.put("itemborders_colors", colors);
        }

        setCustomData(stack, root);
    }

    public static void removeTier(ItemStack itemStack) {
        CompoundTag root = getCustomData(itemStack);
        if (root.contains(Tierify.NBT_SUBTAG_KEY)) {
            CompoundTag tiered = root.getCompound(Tierify.NBT_SUBTAG_KEY).orElse(new CompoundTag());
            Identifier tier = Identifier.parse(tiered.getString(Tierify.NBT_SUBTAG_DATA_KEY).orElse(""));
            PotentialAttribute existingTierAttribute = Tierify.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tier);
            if (existingTierAttribute != null) {
                HashMap<String, Object> nbtMap = existingTierAttribute.getNbtValues();
                List<String> nbtKeys = new ArrayList<>();
                if (nbtMap != null) {
                    nbtKeys.addAll(nbtMap.keySet());
                }

                List<AttributeTemplate> attributeList = existingTierAttribute.getAttributes();
                for (int i = 0; i < attributeList.size(); i++) {
                    String attributeTypeId = attributeList.get(i).getAttributeTypeID();
                    if (CustomEntityAttributes.isDurabilityAttributeId(attributeTypeId)) {
                        nbtKeys.add("durable");
                        break;
                    }
                }

                if (!nbtKeys.isEmpty()) {
                    for (int i = 0; i < nbtKeys.size(); i++) {
                        if (!nbtKeys.get(i).equals("Damage")) {
                            root.remove(nbtKeys.get(i));
                        }
                    }
                }
            }
            root.remove(Tierify.NBT_SUBTAG_KEY);
            root.remove(Tierify.NBT_SUBTAG_MARKER_KEY);
            root.remove(ModifierRollGenerator.GENERATED_ATTRIBUTES_KEY);
            setCustomData(itemStack, root);
            AttributeComponentBuilder.rebuildAttributeModifiersComponent(itemStack);
        }
    }

    public static int applyTierAttributes(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        Identifier tierId = getTierId(stack);
        if (tierId == null) {
            return AttributeComponentBuilder.rebuildAttributeModifiersComponent(stack);
        }

        PotentialAttribute assignedAttribute = Tierify.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tierId);
        if (assignedAttribute == null) {
            Tierify.LOGGER.warn("Tier {} has no attribute template while applying attributes to {}", tierId, BuiltInRegistries.ITEM.getKey(stack.getItem()));
            return AttributeComponentBuilder.rebuildAttributeModifiersComponent(stack);
        }

        if (!hasGeneratedAttributes(tierId)) {
            CompoundTag root = getCustomData(stack);
            clearTierNbtKeys(root, tierId, true);
            setCustomData(stack, root);
            stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
            return 0;
        }

        CompoundTag root = getCustomData(stack);
        clearTierNbtKeys(root, tierId, false);

        List<GeneratedAttributeRoll> generatedRolls = ModifierRollGenerator.readGeneratedRollsFromNbt(root);
        if (generatedRolls.isEmpty()) {
            generatedRolls = ModifierRollGenerator.generateTierAttributeRolls(stack, tierId, assignedAttribute);
        } else {
            generatedRolls = ModifierRollGenerator.normalizeGroupedRolls(generatedRolls);
        }

        ModifierRollGenerator.writeGeneratedRollsToNbt(root, generatedRolls);
        applyTierNbtValues(root, assignedAttribute, tierId, generatedRolls);
        setCustomData(stack, root);
        int appliedCount = AttributeComponentBuilder.rebuildAttributeModifiersComponent(stack);
        if (appliedCount == 0) {
            Tierify.LOGGER.warn("Tier {} generated zero modifiers for {}; item will remain tiered but has no generated tier attributes", tierId, BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
        return appliedCount;
    }

    public static void applyTierById(@Nullable Identifier tierId, ItemStack stack) {
        if (tierId != null) {
            PotentialAttribute assignedAttribute = Tierify.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(Identifier.parse(tierId.toString()));
            if (assignedAttribute == null) {
                Tierify.LOGGER.warn("Tier {} has no attribute template while assigning to {}", tierId, BuiltInRegistries.ITEM.getKey(stack.getItem()));
                return;
            }

            setTier(stack, tierId);
            int appliedCount = applyTierAttributes(stack);
            if (appliedCount == 0 && hasGeneratedAttributes(tierId)) {
                Tierify.LOGGER.warn("No modifiers applied for item={} tier={} despite assignment; reason=pool_empty_or_slot_mismatch_or_invalid_attribute_type", BuiltInRegistries.ITEM.getKey(stack.getItem()), tierId);
            }
        } else {
            Tierify.LOGGER.warn("Tier assignment skipped for {} because no valid tier was selected", BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
    }

    public static void assignOrRepairTier(@Nullable Player playerEntity, ItemStack stack, boolean reforge) {
        CompoundTag customData = getCustomData(stack);
        boolean alreadyTiered = customData.contains(Tierify.NBT_SUBTAG_KEY);
        boolean hasMarker = customData.contains(Tierify.NBT_SUBTAG_MARKER_KEY) && customData.getBoolean(Tierify.NBT_SUBTAG_MARKER_KEY).orElse(false);

        if (alreadyTiered) {
            Identifier existingTier = getTierId(stack);
            if (!hasGeneratedAttributes(existingTier)) {
                applyTierAttributes(stack);
                return;
            }

            int generatedModifierCount = AttributeComponentBuilder.countGeneratedTierModifiers(stack);
            if (existingTier != null && generatedModifierCount > 0) {
                return;
            }

            Tierify.LOGGER.warn("Detected broken tiered item for {} (tier={}, generatedModifierCount={}); attempting repair before regeneration", BuiltInRegistries.ITEM.getKey(stack.getItem()), existingTier, generatedModifierCount);
            int repairedCount = applyTierAttributes(stack);
            if (repairedCount > 0) {
                return;
            }

            Tierify.LOGGER.warn("Repair failed for {} (tier={}); clearing tier data and regenerating", BuiltInRegistries.ITEM.getKey(stack.getItem()), existingTier);
            removeTier(stack);
        } else if (hasMarker) {
            Tierify.LOGGER.warn("Found tier marker without tier data on {}; clearing marker and regenerating", BuiltInRegistries.ITEM.getKey(stack.getItem()));
            customData.remove(Tierify.NBT_SUBTAG_MARKER_KEY);
            setCustomData(stack, customData);
        }

        applyTierById(TierSelector.selectRandomTierId(playerEntity, stack.getItem(), reforge), stack);
    }
}
