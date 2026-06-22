package elocindev.tierify.registry;

import elocindev.tierify.Tierify;
import elocindev.tierify.item.ReforgeAddition;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ItemRegistry {
    
    public static final Item LIMESTONE_CHUNK = registerReforgeItem("limestone_chunk", 1);
    public static final Item RAW_PYRITE = registerReforgeItem("pyrite_chunk", 2);
    public static final Item RAW_GALENA = registerReforgeItem("galena_chunk", 3);

    public static void init() {}

    private static Item registerReforgeItem(String name, int tier) {
        Identifier id = Tierify.id(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Item item = new ReforgeAddition(new Item.Properties().setId(key), tier);
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }
}
