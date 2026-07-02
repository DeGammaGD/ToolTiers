package elocindev.tierify.tier;

import elocindev.tierify.Tierify;
import elocindev.tierify.api.AttributeTemplate;
import elocindev.tierify.api.PotentialAttribute;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings({"null"})
public final class TierSelector {

    private TierSelector() {
    }

    private static <T> void sortByWeight(List<Integer> weights, List<T> values) {
        List<Integer> order = new ArrayList<>(weights.size());
        for (int i = 0; i < weights.size(); i++) {
            order.add(i);
        }

        order.sort(Comparator.comparingInt(weights::get));

        List<Integer> sortedWeights = new ArrayList<>(weights.size());
        List<T> sortedValues = new ArrayList<>(values.size());
        for (int index : order) {
            sortedWeights.add(weights.get(index));
            sortedValues.add(values.get(index));
        }

        weights.clear();
        weights.addAll(sortedWeights);
        values.clear();
        values.addAll(sortedValues);
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

    private static int countUsableModifierTemplates(Item item, PotentialAttribute attribute) {
        if (attribute == null || attribute.getAttributes() == null || attribute.getAttributes().isEmpty()) {
            return 0;
        }

        ItemStack probeStack = new ItemStack(item.builtInRegistryHolder());
        int usable = 0;

        for (AttributeTemplate template : attribute.getAttributes()) {
            if (template == null || template.getEntityAttributeModifier() == null || !isValidAttributeTypeId(template.getAttributeTypeID())) {
                continue;
            }

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!Tierify.isPreferredEquipmentSlot(probeStack, slot)) {
                    continue;
                }

                if (templateAppliesToSlot(template, slot)) {
                    usable++;
                    break;
                }
            }
        }

        return usable;
    }

    /**
     * Returns the ID of a random attribute valid for the given item, or null if there are no valid options.
     */
    @Nullable
    public static Identifier selectRandomTierId(@Nullable Player playerEntity, Item item, boolean reforge) {
        List<Identifier> potentialAttributes = new ArrayList<>();
        List<Integer> attributeWeights = new ArrayList<>();
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);

        Tierify.ATTRIBUTE_DATA_LOADER.getItemAttributes().forEach((id, attribute) -> {
            boolean verifierResult = attribute.isValid(itemId);
            boolean weightAllowed = attribute.getWeight() > 0;
            Identifier candidateId = Identifier.parse(attribute.getID());

            if (!verifierResult || !weightAllowed || isMythicTier(candidateId)) {
                return;
            }

            int modifierPool = countUsableModifierTemplates(item, attribute);
            boolean tierOnlyDefinition = attribute.getAttributes() == null || attribute.getAttributes().isEmpty();
            if (modifierPool > 0 || tierOnlyDefinition) {
                potentialAttributes.add(candidateId);
                attributeWeights.add(attribute.getWeight());
            }
        });

        if (potentialAttributes.isEmpty()) {
            return null;
        }

        if (playerEntity != null) {
            int luckMaxWeight = Collections.max(attributeWeights);
            for (int i = 0; i < attributeWeights.size(); i++) {
                if (attributeWeights.get(i) > luckMaxWeight / 3) {
                    attributeWeights.set(i, (int) (attributeWeights.get(i) * (1.0f - 0.02f * playerEntity.getLuck())));
                }
            }
        }

        int totalWeight = 0;
        for (Integer weight : attributeWeights) {
            totalWeight += weight.intValue();
        }
        int randomChoice = ThreadLocalRandom.current().nextInt(totalWeight);
        sortByWeight(attributeWeights, potentialAttributes);

        for (int i = 0; i < attributeWeights.size(); i++) {
            if (randomChoice < attributeWeights.get(i)) {
                return potentialAttributes.get(i);
            }
            randomChoice -= attributeWeights.get(i);
        }

        return potentialAttributes.get(ThreadLocalRandom.current().nextInt(potentialAttributes.size()));
    }

    private static boolean isMythicTier(Identifier tierId) {
        return tierId != null && tierId.getPath().toLowerCase().contains("mythic");
    }
}
