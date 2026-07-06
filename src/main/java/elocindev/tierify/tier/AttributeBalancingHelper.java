package elocindev.tierify.tier;

import java.util.Map;

/**
 * Tierify-specific attribute balancing utilities applied at attribute build time.
 */
public final class AttributeBalancingHelper {

    private static final String MOVEMENT_SPEED_ATTRIBUTE_ID = "minecraft:movement_speed";

    private AttributeBalancingHelper() {
    }

    /**
     * Applies diminishing returns to Tierify-generated movement speed modifiers only.
     * First modifier uses full value, each subsequent modifier contributes half of the previous one:
     * amount * pow(0.5, index), where index starts at 0.
     */
    public static double applyTierifyMovementSpeedDiminishingReturns(String attributeTypeId,
                                                                      double amount,
                                                                      Map<String, Integer> diminishingCounters) {
        if (!MOVEMENT_SPEED_ATTRIBUTE_ID.equals(attributeTypeId)) {
            return amount;
        }

        int index = diminishingCounters.getOrDefault(MOVEMENT_SPEED_ATTRIBUTE_ID, 0);
        diminishingCounters.put(MOVEMENT_SPEED_ATTRIBUTE_ID, index + 1);
        return amount * Math.pow(0.5D, index);
    }
}
