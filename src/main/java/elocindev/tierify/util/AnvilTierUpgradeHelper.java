package elocindev.tierify.util;

import elocindev.tierify.tier.TierSelector;
import elocindev.tierify.tier.TierManager;
import elocindev.tierify.Tierify;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class AnvilTierUpgradeHelper {

    private static final Identifier TOTEM_OF_UNDYING_ID = Identifier.fromNamespaceAndPath("minecraft", "totem_of_undying");
    private static final Identifier NETHER_STAR_ID = Identifier.fromNamespaceAndPath("minecraft", "nether_star");
    private static final Identifier ECHO_SHARD_ID = Identifier.fromNamespaceAndPath("minecraft", "echo_shard");

    private AnvilTierUpgradeHelper() {
    }

    public static void applyTierUpgradeResultIfNeeded(Player player, AnvilMenu menu, ItemStack resultStack) {
        ItemStack left = menu.getSlot(0).getItem();
        ItemStack right = menu.getSlot(1).getItem();

        if (left.isEmpty() || right.isEmpty() || resultStack.isEmpty()) {
            return;
        }
        if (isTotemReforgeOperationValid(left, right)) {
            Identifier leftTier = TierManager.getTier(left);
            Identifier rightTier = TierManager.getTier(right);
            if (leftTier == null || rightTier == null) {
                return;
            }

            Identifier targetTier = buildTierVariant(leftTier, getTierQuality(rightTier));
            if (targetTier == null) {
                return;
            }

            finalizeReforgeResult(player, menu, resultStack, targetTier);
            return;
        }

        if (isNetherStarMythicReforgeOperationValid(left, right)) {
            Identifier leftTier = TierManager.getTier(left);
            if (leftTier == null) {
                return;
            }

            finalizeReforgeResult(player, menu, resultStack, buildTierVariant(leftTier, "mythic"));
            return;
        }

        if (isEchoShardRerollOperationValid(left, right)) {
            Identifier leftTier = TierManager.getTier(left);
            if (leftTier == null) {
                return;
            }

            finalizeRerollResult(player, menu, resultStack, leftTier);
            return;
        }

        if (left.getItem() != right.getItem() || resultStack.getItem() != left.getItem()) {
            return;
        }

        Identifier leftTier = TierManager.getTier(left);
        Identifier rightTier = TierManager.getTier(right);
        if (leftTier == null || rightTier == null) {
            return;
        }
        if (!isTierUpgradeOperationValid(left, right)) {
            return;
        }

        Identifier targetTier = pickResultTier(left.getItem(), leftTier, rightTier);
        if (targetTier == null) {
            return;
        }

        finalizeReforgeResult(player, menu, resultStack, targetTier);
    }

    public static boolean isTotemReforgeOperationValid(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        if (!TOTEM_OF_UNDYING_ID.equals(BuiltInRegistries.ITEM.getKey(right.getItem()))) {
            return false;
        }

        return TierManager.getTier(left) != null && TierManager.getTier(right) != null;
    }

    public static boolean isNetherStarMythicReforgeOperationValid(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        if (!NETHER_STAR_ID.equals(BuiltInRegistries.ITEM.getKey(right.getItem()))) {
            return false;
        }

        Identifier leftTier = TierManager.getTier(left);
        if (leftTier == null) {
            return false;
        }

        TierRank leftRank = rankForTierId(leftTier);
        return leftRank == TierRank.LEGENDARY;
    }

    public static boolean isEchoShardRerollOperationValid(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        if (!ECHO_SHARD_ID.equals(BuiltInRegistries.ITEM.getKey(right.getItem()))) {
            return false;
        }

        return TierManager.getTier(left) != null;
    }

    public static Identifier buildTierVariant(Identifier sourceTier, String quality) {
        if (sourceTier == null || quality == null || quality.isBlank()) {
            return null;
        }

        String path = sourceTier.getPath();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 0) {
            return null;
        }

        String category = path.substring(0, lastSlash);
        return Identifier.fromNamespaceAndPath(sourceTier.getNamespace(), category + "/" + quality.toLowerCase());
    }

    public static String getTierQuality(Identifier tierId) {
        if (tierId == null) {
            return null;
        }

        TierRank rank = rankForTierId(tierId);
        if (rank == null) {
            return null;
        }

        return switch (rank) {
            case COMMON -> "common";
            case UNCOMMON -> "uncommon";
            case RARE -> "rare";
            case EPIC -> "epic";
            case LEGENDARY -> "legendary";
            case MYTHIC -> "mythic";
        };
    }

    public static Identifier pickResultTier(Item item, Identifier leftTier, Identifier rightTier) {
        TierRank leftRank = rankForTierId(leftTier);
        TierRank rightRank = rankForTierId(rightTier);
        if (leftRank == null || rightRank == null) {
            return null;
        }

        Map<TierRank, List<Identifier>> tiersByRank = collectTiersByRank(item);

        if (tiersByRank.isEmpty()) {
            return null;
        }

        TierRank targetRank;
        if (leftRank == rightRank) {
            targetRank = nextRank(leftRank);
        } else {
            targetRank = higherRank(leftRank, rightRank);
        }

        List<Identifier> candidates = tiersByRank.get(targetRank);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        if (leftRank != rightRank) {
            return leftRank.ordinal() >= rightRank.ordinal() ? leftTier : rightTier;
        }

        if (targetRank == leftRank) {
            return leftTier;
        }

        return chooseDeterministicTier(candidates, leftTier);
    }

    public static boolean isTierUpgradeOperationValid(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        if (isTotemReforgeOperationValid(left, right)) {
            return true;
        }
        if (left.getItem() != right.getItem()) {
            return false;
        }

        Identifier leftTier = TierManager.getTier(left);
        Identifier rightTier = TierManager.getTier(right);
        if (leftTier == null || rightTier == null) {
            return false;
        }

        return hasUpgradeableTarget(left.getItem(), leftTier, rightTier);
    }

    public static boolean isStandardReforgeOperationValid(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        if (left.getItem() != right.getItem()) {
            return false;
        }

        Identifier leftTier = TierManager.getTier(left);
        Identifier rightTier = TierManager.getTier(right);
        if (leftTier == null || rightTier == null) {
            return false;
        }

        return hasUpgradeableTarget(left.getItem(), leftTier, rightTier);
    }

    private static boolean hasUpgradeableTarget(Item item, Identifier leftTier, Identifier rightTier) {
        TierRank leftRank = rankForTierId(leftTier);
        TierRank rightRank = rankForTierId(rightTier);
        if (leftRank == null || rightRank == null) {
            return false;
        }

        Map<TierRank, List<Identifier>> tiersByRank = collectTiersByRank(item);
        if (tiersByRank.isEmpty()) {
            return false;
        }

        if (leftRank == rightRank) {
            TierRank targetRank = nextRank(leftRank);
            List<Identifier> candidates = tiersByRank.get(targetRank);
            return candidates != null && !candidates.isEmpty();
        }

        TierRank targetRank = higherRank(leftRank, rightRank);
        List<Identifier> candidates = tiersByRank.get(targetRank);
        return candidates != null && !candidates.isEmpty();
    }

    private static Map<TierRank, List<Identifier>> collectTiersByRank(Item item) {
        Map<TierRank, List<Identifier>> tiersByRank = new EnumMap<>(TierRank.class);
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);

        Tierify.ATTRIBUTE_DATA_LOADER.getItemAttributes().forEach((id, attribute) -> {
            if (!attribute.isValid(itemId)) {
                return;
            }
            TierRank rank = rankForTierId(id);
            if (rank != null) {
                tiersByRank.computeIfAbsent(rank, ignored -> new ArrayList<>()).add(id);
            }
        });

        return tiersByRank;
    }

    private static Identifier chooseDeterministicTier(List<Identifier> candidates, Identifier preferredSourceTier) {
        Identifier fallback = null;
        Identifier preferred = null;
        String preferredKey = tierInvariantKey(preferredSourceTier);

        for (Identifier candidate : candidates) {
            if (fallback == null || compareIdentifier(candidate, fallback) < 0) {
                fallback = candidate;
            }

            if (preferredKey.equals(tierInvariantKey(candidate))) {
                if (preferred == null || compareIdentifier(candidate, preferred) < 0) {
                    preferred = candidate;
                }
            }
        }

        return preferred != null ? preferred : fallback;
    }

    private static TierRank higherRank(TierRank first, TierRank second) {
        return first.ordinal() >= second.ordinal() ? first : second;
    }

    private static int compareIdentifier(Identifier first, Identifier second) {
        int namespaceCompare = first.getNamespace().compareTo(second.getNamespace());
        if (namespaceCompare != 0) {
            return namespaceCompare;
        }
        return first.getPath().compareTo(second.getPath());
    }

    private static String tierInvariantKey(Identifier id) {
        return id.getNamespace() + ":" + id.getPath().toLowerCase()
                .replace("uncommon", "")
                .replace("legendary", "")
                .replace("common", "")
                .replace("mythic", "")
                .replace("epic", "")
                .replace("rare", "");
    }

    private static TierRank rankForTierId(Identifier id) {
        String path = id.getPath().toLowerCase();
        if (path.contains("uncommon")) {
            return TierRank.UNCOMMON;
        }
        if (path.contains("legendary")) {
            return TierRank.LEGENDARY;
        }
        if (path.contains("common")) {
            return TierRank.COMMON;
        }
        if (path.contains("mythic")) {
            return TierRank.MYTHIC;
        }
        if (path.contains("epic")) {
            return TierRank.EPIC;
        }
        if (path.contains("rare")) {
            return TierRank.RARE;
        }
        return null;
    }

    private static TierRank nextRank(TierRank rank) {
        if (rank == TierRank.LEGENDARY) {
            return TierRank.LEGENDARY;
        }
        if (rank == TierRank.MYTHIC) {
            return TierRank.MYTHIC;
        }
        return TierRank.values()[rank.ordinal() + 1];
    }

    private static void finalizeReforgeResult(Player player, AnvilMenu menu, ItemStack resultStack, Identifier targetTier) {
        TierManager.removeTier(resultStack);
        TierManager.setTier(resultStack, targetTier);
        TierManager.repairTier(resultStack);
        TierManager.rebuildAttributes(resultStack);

        menu.getSlot(2).set(resultStack);
        menu.getSlot(2).setChanged();
        player.getInventory().setChanged();
        menu.broadcastChanges();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
            serverPlayer.inventoryMenu.broadcastChanges();
        }
    }

    private static void finalizeRerollResult(Player player, AnvilMenu menu, ItemStack resultStack, Identifier tierId) {
        TierManager.removeTier(resultStack);
        TierManager.setTier(resultStack, tierId);
        TierManager.repairTier(resultStack);
        TierManager.rebuildAttributes(resultStack);

        menu.getSlot(2).set(resultStack);
        menu.getSlot(2).setChanged();
        player.getInventory().setChanged();
        menu.broadcastChanges();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
            serverPlayer.inventoryMenu.broadcastChanges();
        }
    }

    private enum TierRank {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY,
        MYTHIC
    }
}
