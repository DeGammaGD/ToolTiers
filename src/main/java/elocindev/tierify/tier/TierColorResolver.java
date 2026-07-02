package elocindev.tierify.tier;

import elocindev.tierify.Tierify;
import net.minecraft.resources.Identifier;

import java.util.Locale;

@SuppressWarnings({"null"})
final class TierColorResolver {

    private TierColorResolver() {
    }

    static String getColorForTier(Identifier tierId) {
        var attribute = Tierify.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tierId);
        if (attribute == null) return null;

        String path = tierId.getPath().toLowerCase(Locale.ROOT);
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
