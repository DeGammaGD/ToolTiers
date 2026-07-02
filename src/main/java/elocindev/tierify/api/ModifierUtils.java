package elocindev.tierify.api;

import com.google.common.collect.Multimap;
import elocindev.tierify.tier.AttributeComponentBuilder;
import elocindev.tierify.tier.TierManager;
import elocindev.tierify.tier.TierSelector;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Compatibility facade kept for existing integrations.
 * New project code should use TierManager.
 */
@Deprecated(forRemoval = false)
@SuppressWarnings({"null"})
public final class ModifierUtils {

    private ModifierUtils() {
    }

    public static boolean hasTier(ItemStack stack) {
        return TierManager.hasTier(stack);
    }

    public static boolean hasTierMarker(ItemStack stack) {
        return TierManager.hasTierMarker(stack);
    }

    public static void applyTierIfNeeded(ItemStack stack) {
        TierManager.applyTierIfNeeded(stack);
    }

    @Nullable
    public static Identifier getRandomAttributeIDFor(@Nullable Player playerEntity, Item item, boolean reforge) {
        return TierSelector.selectRandomTierId(playerEntity, item, reforge);
    }

    public static void setItemStackAttribute(Identifier potentialAttributeID, ItemStack stack) {
        TierManager.applyTier(stack, potentialAttributeID);
    }

    public static void setItemStackAttribute(@Nullable Player playerEntity, ItemStack stack, boolean reforge) {
        TierManager.assignTierWithRepair(playerEntity, stack, reforge);
    }

    public static void setTier(ItemStack stack, Identifier tierId) {
        TierManager.setTier(stack, tierId);
    }

    public static int applyTierAttributes(ItemStack stack) {
        return TierManager.repairTier(stack);
    }

    public static void removeItemStackAttribute(ItemStack itemStack) {
        TierManager.removeTier(itemStack);
    }

    @Nullable
    public static Identifier getAttributeID(ItemStack itemStack) {
        return TierManager.getTier(itemStack);
    }

    public static Multimap<Holder<Attribute>, AttributeModifier> buildTierAttributeMap(ItemStack itemStack, EquipmentSlot slot) {
        return AttributeComponentBuilder.buildTierAttributeMap(itemStack, slot);
    }

    public static int rebuildAttributeModifiersComponent(ItemStack itemStack) {
        return TierManager.rebuildAttributes(itemStack);
    }
}