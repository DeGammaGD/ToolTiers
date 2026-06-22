package elocindev.tierify.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

public class ReforgeDataLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger("TieredZ");

    private List<Identifier> reforgeIdentifiers = new ArrayList<>();
    private Map<Identifier, List<Item>> reforgeBaseMap = new HashMap<>();

    @Override
    public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath("tiered", "reforge_loader");
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        reforgeIdentifiers.clear();
        reforgeBaseMap.clear();
        LOGGER.info("Reloading reforge item definitions");

        resourceManager.listResources("reforge_items", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                try (InputStream stream = resourceRef.open(); InputStreamReader reader = new InputStreamReader(stream)) {
                    JsonObject data = JsonParser.parseReader(reader).getAsJsonObject();

                    for (int u = 0; u < data.getAsJsonArray("items").size(); u++) {
                        List<Item> baseItems = new ArrayList<Item>();
                        for (int i = 0; i < data.getAsJsonArray("base").size(); i++) {
                            if (BuiltInRegistries.ITEM.getValue(Identifier.parse(data.getAsJsonArray("base").get(i).getAsString())).toString().equals("air")) {
                                LOGGER.info("Resource {} was not loaded cause {} is not a valid item identifier", id.toString(), data.getAsJsonArray("base").get(i).getAsString());
                                continue;
                            }
                            baseItems.add(BuiltInRegistries.ITEM.getValue(Identifier.parse(data.getAsJsonArray("base").get(i).getAsString())));
                        }
                        if (BuiltInRegistries.ITEM.getValue(Identifier.parse(data.getAsJsonArray("items").get(u).getAsString())).toString().equals("air")) {
                            LOGGER.info("Resource {} was not loaded cause {} is not a valid item identifier", id.toString(), data.getAsJsonArray("items").get(u).getAsString());
                            continue;
                        }
                        reforgeIdentifiers.add(Identifier.parse(data.getAsJsonArray("items").get(u).getAsString()));
                        reforgeBaseMap.put(Identifier.parse(data.getAsJsonArray("items").get(u).getAsString()), baseItems);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
        LOGGER.info("Loaded {} reforge item definitions", reforgeIdentifiers.size());
    }

    public List<Item> getReforgeBaseItems(Item item) {
        ArrayList<Item> list = new ArrayList<Item>();
        if (reforgeBaseMap.containsKey(BuiltInRegistries.ITEM.getKey(item))) {
            return reforgeBaseMap.get(BuiltInRegistries.ITEM.getKey(item));
        }
        return list;
    }

    public void putReforgeBaseItems(Identifier id, List<Item> items) {
        reforgeBaseMap.put(id, items);
    }

    public void clearReforgeBaseItems() {
        reforgeBaseMap.clear();
    }

    public List<Identifier> getReforgeIdentifiers() {
        return reforgeIdentifiers;
    }

}
