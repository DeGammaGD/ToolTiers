package elocindev.tierify.compat;

import elocindev.tierify.Tierify;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

@SuppressWarnings({"null"})
public class ItemBordersCompat {
    
    public static void addBorder(ItemStack stack, String color) {
        CustomData component = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = component != null ? component.copyTag() : new CompoundTag();
        CompoundTag nbt = root.contains("itemborders_colors") ? root.getCompound("itemborders_colors").orElse(new CompoundTag()) : new CompoundTag();
        nbt.putString("top", color);
        root.put("itemborders_colors", nbt);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    public static void addBorder(ItemStack stack, String topColor, String bottomColor) {
        CustomData component = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = component != null ? component.copyTag() : new CompoundTag();
        CompoundTag nbt = root.contains("itemborders_colors") ? root.getCompound("itemborders_colors").orElse(new CompoundTag()) : new CompoundTag();
        nbt.putString("top", topColor);
        nbt.putString("bottom", bottomColor);
        root.put("itemborders_colors", nbt);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    /*
     * Returns the hex color string for a tier identifier by extracting the quality from the identifier
     * path (e.g. "tiered:melee_weapons/rare" → "rare"). Falls back to the style color if set, or null.
     */
    public static String getColorForIdentifier(Identifier identifier) {
        var attribute = Tierify.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(identifier);
        if (attribute == null) return null;

        // Extract quality from the identifier path directly — avoids a client-side translation lookup
        // on the server write path and works even when language files are not loaded.
        String path = identifier.getPath().toLowerCase(java.util.Locale.ROOT);
        if (path.contains("mythic"))     return "0xb53f3f";
        if (path.contains("legendary"))  return "0xcf9e44";
        if (path.contains("epic"))       return "0xa762c4";
        if (path.contains("rare"))       return "0x6293c4";
        if (path.contains("uncommon"))   return "0x76c462";
        if (path.contains("common"))     return "0xc7c7c7";

        var style = attribute.getStyle();
        if (style != null && style.getColor() != null) {
            return String.valueOf(style.getColor().getValue());
        }
        return null;
    }
}