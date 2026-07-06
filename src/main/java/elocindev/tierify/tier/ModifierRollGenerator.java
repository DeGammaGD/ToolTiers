package elocindev.tierify.tier;

import elocindev.tierify.Tierify;
import elocindev.tierify.api.AttributeTemplate;
import elocindev.tierify.api.CustomEntityAttributes;
import elocindev.tierify.api.PotentialAttribute;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings({"null"})
public final class ModifierRollGenerator {

    static final String GENERATED_ATTRIBUTES_KEY = "TieredGeneratedAttributes";
    static final String GENERATED_COUNT_KEY = "count";
    static final String ENTRY_PREFIX = "entry_";

    private ModifierRollGenerator() {
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

    private static boolean templateAppliesToSlot(AttributeTemplate template, EquipmentSlot slot) {
        return slotArrayContains(template.getRequiredEquipmentSlots(), slot)
                || slotArrayContains(template.getOptionalEquipmentSlots(), slot);
    }

    private static boolean slotArrayContains(@Nullable EquipmentSlot[] slots, EquipmentSlot slot) {
        if (slots == null) return false;
        for (EquipmentSlot s : slots) {
            if (s == slot) return true;
        }
        return false;
    }

    private static String resolveTierQuality(Identifier tierId) {
        String path = tierId.getPath().toLowerCase(Locale.ROOT);
        if (path.contains("uncommon")) {
            return "uncommon";
        }
        if (path.contains("legendary")) {
            return "legendary";
        }
        if (path.contains("mythic")) {
            return "mythic";
        }
        if (path.contains("common")) {
            return "common";
        }
        if (path.contains("rare")) {
            return "rare";
        }
        if (path.contains("epic")) {
            return "epic";
        }
        return "common";
    }

    private static int[] getTierAttributeBounds(Identifier tierId) {
        return switch (resolveTierQuality(tierId)) {
            case "uncommon" -> new int[] {1, 2};
            case "rare" -> new int[] {2, 3};
            case "epic" -> new int[] {3, 3};
            case "legendary" -> new int[] {3, 4};
            case "mythic" -> new int[] {4, 4};
            default -> new int[] {1, 1};
        };
    }

    private static double getTierQualityCenter(Identifier tierId) {
        return switch (resolveTierQuality(tierId)) {
            case "uncommon" -> 0.45D;
            case "rare" -> 0.60D;
            case "epic" -> 0.72D;
            case "legendary" -> 0.84D;
            case "mythic" -> 0.92D;
            default -> 0.30D;
        };
    }

    private static double clamp01(double value) {
        if (value < 0.0D) {
            return 0.0D;
        }
        if (value > 1.0D) {
            return 1.0D;
        }
        return value;
    }

    private static double rollAmountForTier(double minAmount, double maxAmount, Identifier tierId) {
        if (Math.abs(maxAmount - minAmount) < 0.0000001D) {
            return minAmount;
        }

        double center = getTierQualityCenter(tierId);
        double base = ThreadLocalRandom.current().nextDouble();
        double jitter = (ThreadLocalRandom.current().nextDouble() - 0.5D) * 0.2D;
        double quality = clamp01((base * 0.65D) + (center * 0.35D) + jitter);
        return minAmount + (maxAmount - minAmount) * quality;
    }

    private static String getModifierBaseId(AttributeTemplate template, Identifier tierId) {
        if (template != null && template.getEntityAttributeModifier() != null && template.getEntityAttributeModifier().id() != null) {
            return template.getEntityAttributeModifier().id().toString();
        }
        return tierId + "_" + System.nanoTime();
    }

    private static EquipmentSlot[] copySlots(EquipmentSlot[] slots) {
        if (slots == null) {
            return null;
        }
        EquipmentSlot[] copy = new EquipmentSlot[slots.length];
        System.arraycopy(slots, 0, copy, 0, slots.length);
        return copy;
    }

    private static String resolveGroupedAttributeType(String attributeTypeId) {
        if (!CustomEntityAttributes.isProtectionFamilyAttributeId(attributeTypeId)) {
            return attributeTypeId;
        }

        String[] family = CustomEntityAttributes.PROTECTION_FAMILY_MEMBERS;
        if (family.length == 0) {
            return attributeTypeId;
        }

        return family[ThreadLocalRandom.current().nextInt(family.length)];
    }

    public static List<GeneratedAttributeRoll> normalizeGroupedRolls(List<GeneratedAttributeRoll> sourceRolls) {
        if (sourceRolls == null || sourceRolls.isEmpty()) {
            return sourceRolls;
        }

        List<GeneratedAttributeRoll> normalized = new ArrayList<>(sourceRolls.size());
        for (GeneratedAttributeRoll roll : sourceRolls) {
            if (roll == null) {
                continue;
            }

            String resolvedTypeId = resolveGroupedAttributeType(roll.attributeTypeId);
            normalized.add(new GeneratedAttributeRoll(
                    resolvedTypeId,
                    roll.modifierId,
                    roll.operation,
                    roll.amount,
                    copySlots(roll.requiredSlots),
                    copySlots(roll.optionalSlots)
            ));
        }

        return normalized;
    }

    public static boolean rollAppliesToSlot(GeneratedAttributeRoll roll, EquipmentSlot slot) {
        if (roll == null) {
            return false;
        }

        if (roll.requiredSlots != null) {
            for (EquipmentSlot requiredSlot : roll.requiredSlots) {
                if (requiredSlot == slot) {
                    return true;
                }
            }
        }

        if (roll.optionalSlots != null) {
            for (EquipmentSlot optionalSlot : roll.optionalSlots) {
                if (optionalSlot == slot) {
                    return true;
                }
            }
        }

        return false;
    }

    public static List<GeneratedAttributeRoll> generateTierAttributeRolls(ItemStack stack,
                                                                           Identifier tierId,
                                                                           PotentialAttribute assignedAttribute) {
        List<GeneratedAttributeRoll> generated = new ArrayList<>();
        if (assignedAttribute == null || assignedAttribute.getAttributes() == null || assignedAttribute.getAttributes().isEmpty()) {
            return generated;
        }

        Map<String, List<AttributeTemplate>> templatesByType = new LinkedHashMap<>();
        Map<String, String> exclusiveGroupByType = new LinkedHashMap<>();
        for (AttributeTemplate template : assignedAttribute.getAttributes()) {
            if (template == null || template.getEntityAttributeModifier() == null || !isValidAttributeTypeId(template.getAttributeTypeID())) {
                continue;
            }

            boolean usableForItem = false;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!Tierify.isPreferredEquipmentSlot(stack, slot)) {
                    continue;
                }

                if (templateAppliesToSlot(template, slot)) {
                    usableForItem = true;
                    break;
                }
            }

            if (!usableForItem) {
                continue;
            }

            templatesByType.computeIfAbsent(template.getAttributeTypeID(), k -> new ArrayList<>()).add(template);
            if (!exclusiveGroupByType.containsKey(template.getAttributeTypeID())
                    && template.getExclusiveGroup() != null
                    && !template.getExclusiveGroup().isBlank()) {
                exclusiveGroupByType.put(template.getAttributeTypeID(), template.getExclusiveGroup());
            }
        }

        if (templatesByType.isEmpty()) {
            return generated;
        }

        int[] bounds = getTierAttributeBounds(tierId);
        int minCount = Math.max(1, bounds[0]);
        int maxCount = Math.max(minCount, bounds[1]);

        int available = templatesByType.size();
        minCount = Math.min(minCount, available);
        maxCount = Math.min(maxCount, available);
        int targetCount = ThreadLocalRandom.current().nextInt(minCount, maxCount + 1);

        List<String> types = new ArrayList<>(templatesByType.keySet());
        Collections.shuffle(types);
        List<String> selectedTypes = new ArrayList<>();
        List<String> selectedExclusiveGroups = new ArrayList<>();

        for (String type : types) {
            if (selectedTypes.size() >= targetCount) {
                break;
            }

            String exclusiveGroup = exclusiveGroupByType.get(type);
            if (exclusiveGroup != null && selectedExclusiveGroups.contains(exclusiveGroup)) {
                continue;
            }

            selectedTypes.add(type);
            if (exclusiveGroup != null) {
                selectedExclusiveGroups.add(exclusiveGroup);
            }
        }

        for (String type : selectedTypes) {
            List<AttributeTemplate> group = templatesByType.get(type);
            if (group == null || group.isEmpty()) {
                continue;
            }

            AttributeTemplate representative = group.get(ThreadLocalRandom.current().nextInt(group.size()));
            double minAmount = representative.getEntityAttributeModifier().amount();
            double maxAmount = minAmount;
            for (AttributeTemplate candidate : group) {
                double amount = candidate.getEntityAttributeModifier().amount();
                if (amount < minAmount) {
                    minAmount = amount;
                }
                if (amount > maxAmount) {
                    maxAmount = amount;
                }
            }

            double rolledAmount = rollAmountForTier(minAmount, maxAmount, tierId);
            generated.add(new GeneratedAttributeRoll(
                    resolveGroupedAttributeType(type),
                    getModifierBaseId(representative, tierId),
                    representative.getEntityAttributeModifier().operation(),
                    rolledAmount,
                    copySlots(representative.getRequiredEquipmentSlots()),
                    copySlots(representative.getOptionalEquipmentSlots())
            ));
        }

        return generated;
    }

    public static void writeGeneratedRollsToNbt(CompoundTag root, List<GeneratedAttributeRoll> generatedRolls) {
        root.remove(GENERATED_ATTRIBUTES_KEY);
        if (generatedRolls == null || generatedRolls.isEmpty()) {
            return;
        }

        CompoundTag generatedTag = new CompoundTag();
        generatedTag.putInt(GENERATED_COUNT_KEY, generatedRolls.size());

        for (int i = 0; i < generatedRolls.size(); i++) {
            GeneratedAttributeRoll roll = generatedRolls.get(i);
            CompoundTag entry = new CompoundTag();
            entry.putString("type", roll.attributeTypeId);
            entry.putString("modifier_id", roll.modifierId);
            entry.putString("operation", roll.operation.name());
            entry.putDouble("amount", roll.amount);

            if (roll.requiredSlots != null && roll.requiredSlots.length > 0) {
                StringBuilder requiredBuilder = new StringBuilder();
                for (int s = 0; s < roll.requiredSlots.length; s++) {
                    if (s > 0) {
                        requiredBuilder.append(',');
                    }
                    requiredBuilder.append(roll.requiredSlots[s].name());
                }
                entry.putString("required_slots", requiredBuilder.toString());
            }

            if (roll.optionalSlots != null && roll.optionalSlots.length > 0) {
                StringBuilder optionalBuilder = new StringBuilder();
                for (int s = 0; s < roll.optionalSlots.length; s++) {
                    if (s > 0) {
                        optionalBuilder.append(',');
                    }
                    optionalBuilder.append(roll.optionalSlots[s].name());
                }
                entry.putString("optional_slots", optionalBuilder.toString());
            }

            generatedTag.put(ENTRY_PREFIX + i, entry);
        }

        root.put(GENERATED_ATTRIBUTES_KEY, generatedTag);
    }

    private static EquipmentSlot[] parseSlotsCsv(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String[] raw = value.split(",");
        List<EquipmentSlot> parsed = new ArrayList<>();
        for (String token : raw) {
            if (token == null || token.isBlank()) {
                continue;
            }
            try {
                parsed.add(EquipmentSlot.valueOf(token.trim()));
            } catch (IllegalArgumentException ignored) {
                // ignore invalid slot token
            }
        }

        if (parsed.isEmpty()) {
            return null;
        }

        return parsed.toArray(new EquipmentSlot[0]);
    }

    public static List<GeneratedAttributeRoll> readGeneratedRollsFromNbt(CompoundTag root) {
        List<GeneratedAttributeRoll> generated = new ArrayList<>();
        if (!root.contains(GENERATED_ATTRIBUTES_KEY)) {
            return generated;
        }

        CompoundTag generatedTag = root.getCompound(GENERATED_ATTRIBUTES_KEY).orElse(new CompoundTag());
        int count = generatedTag.getInt(GENERATED_COUNT_KEY).orElse(0);
        for (int i = 0; i < count; i++) {
            CompoundTag entry = generatedTag.getCompound(ENTRY_PREFIX + i).orElse(null);
            if (entry == null) {
                continue;
            }

            String type = entry.getString("type").orElse("");
            String modifierId = entry.getString("modifier_id").orElse("");
            String operationRaw = entry.getString("operation").orElse("");
            double amount = entry.getDouble("amount").orElse(0.0D);

            if (type.isBlank() || modifierId.isBlank() || operationRaw.isBlank()) {
                continue;
            }

            AttributeModifier.Operation operation;
            try {
                operation = AttributeModifier.Operation.valueOf(operationRaw);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            EquipmentSlot[] required = parseSlotsCsv(entry.getString("required_slots").orElse(""));
            EquipmentSlot[] optional = parseSlotsCsv(entry.getString("optional_slots").orElse(""));
            generated.add(new GeneratedAttributeRoll(type, modifierId, operation, amount, required, optional));
        }

        return generated;
    }

    public static boolean isMasteryAttributeType(String attributeTypeId) {
        return attributeTypeId != null && attributeTypeId.toLowerCase(Locale.ROOT).contains("mastery");
    }

    public static double getMasteryMultiplierForSlot(List<GeneratedAttributeRoll> generatedRolls, EquipmentSlot slot) {
        if (generatedRolls == null || generatedRolls.isEmpty()) {
            return 0.0D;
        }

        double mastery = 0.0D;
        for (GeneratedAttributeRoll roll : generatedRolls) {
            if (roll == null || !isMasteryAttributeType(roll.attributeTypeId)) {
                continue;
            }

            if (!rollAppliesToSlot(roll, slot)) {
                continue;
            }

            mastery += roll.amount;
        }

        return mastery;
    }

    public static double resolveDurableAmount(List<GeneratedAttributeRoll> generatedRolls, PotentialAttribute assignedAttribute) {
        if (generatedRolls != null) {
            for (GeneratedAttributeRoll roll : generatedRolls) {
                if (roll != null && CustomEntityAttributes.isDurabilityAttributeId(roll.attributeTypeId)) {
                    return (double) Math.round(roll.amount * 100.0D) / 100.0D;
                }
            }
        }

        if (assignedAttribute != null && assignedAttribute.getAttributes() != null) {
            for (AttributeTemplate template : assignedAttribute.getAttributes()) {
                if (template != null && CustomEntityAttributes.isDurabilityAttributeId(template.getAttributeTypeID()) && template.getEntityAttributeModifier() != null) {
                    return (double) Math.round(template.getEntityAttributeModifier().amount() * 100.0D) / 100.0D;
                }
            }
        }

        return 0.0D;
    }
}
