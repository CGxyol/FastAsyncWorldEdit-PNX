package com.sk89q.pnx.util.mappings;

import cn.nukkit.item.Item;
import com.google.common.collect.HashBiMap;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.pnx.PNXWorldEditPlugin;
import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.world.biome.BiomeType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class JEBEMappings119 {

    private static final GsonBuilder builder = new GsonBuilder();
    public static final Map<String, cn.nukkit.blockstate.BlockState> BLOCKS_MAPPING1 = new Object2ObjectOpenHashMap<>();
    public static final Map<cn.nukkit.blockstate.BlockState, String> BLOCKS_MAPPING2 = new Object2ObjectOpenHashMap<>();

    public static final HashBiMap<cn.nukkit.blockstate.BlockState, com.sk89q.worldedit.world.block.BlockState> BLOCKS_MAPPING_CACHE =
            HashBiMap.create();
    public static final HashBiMap<HashItem, com.sk89q.worldedit.world.item.ItemType> ITEMS_MAPPING =
            HashBiMap.create();
    public static final HashBiMap<cn.nukkit.level.biome.Biome, com.sk89q.worldedit.world.biome.BiomeType> BIOMES_MAPPING =
            HashBiMap.create();

    static {
        Map<String, Map<String, Object>> blocks;
        try {
            blocks = builder.create().fromJson(new InputStreamReader(PNXWorldEditPlugin.
                    getInstance().
                    getInternalPlatform().
                    getResourceLoader().
                    getRootResource("mappings/blocks.json").openStream()), Map.class);
            blocks.forEach((k, v) -> {
                var name = v.get("bedrock_identifier").toString();
                var nkState = new StringBuilder();
                if (v.containsKey("bedrock_states")) {
                    Map<String, Object> states = (Map<String, Object>) v.get("bedrock_states");
                    states.forEach((key, value) -> {
                        if (value.toString().equals("true") || value.toString().equals("false")) {
                            nkState.append(";").append(key).append("=").append(value.toString().equals("true") ? 1 : 0);
                        } else if (v instanceof Number number) {
                            nkState.append(";").append(key).append("=").append(number.intValue());
                        } else {
                            try {
                                int i = Double.valueOf(value.toString()).intValue();
                                nkState.append(";").append(key).append("=").append(i);
                            } catch (NumberFormatException e) {
                                nkState.append(";").append(key).append("=").append(value);
                            }
                        }
                    });
                }
                JEBEMappings119.BLOCKS_MAPPING1.put(k, cn.nukkit.blockstate.BlockState.of(name + nkState));
                JEBEMappings119.BLOCKS_MAPPING2.put(cn.nukkit.blockstate.BlockState.of(name + nkState), k);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() {
        final ResourceLoader resourceLoader = PNXWorldEditPlugin.getInstance().getInternalPlatform().getResourceLoader();

        try {
            URL resource = resourceLoader.getRootResource("mappings/items.json");
            if (resource != null) {
                try (InputStream stream = resource.openStream()) {
                    Map<String, Map<String, ?>> map2 = builder.create().fromJson(new InputStreamReader(stream), Map.class);
                    map2.forEach((k, v) -> {
                        var nkItem = v.get("bedrock_identifier").toString();
                        int damage = 0;
                        if (v.containsKey("bedrock_data")) {
                            damage = Double.valueOf(v.get("bedrock_data").toString()).intValue();
                        }
                        ITEMS_MAPPING.put(
                                HashItem.of(Item.fromString(nkItem + ":" + damage)),
                                new com.sk89q.worldedit.world.item.ItemType(k)
                        );
                    });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            URL resource = resourceLoader.getRootResource("mappings/biomes.json");
            if (resource != null) {
                try (InputStream stream = resource.openStream()) {
                    Map<String, Map<String, ?>> map3 = builder.create().fromJson(new InputStreamReader(stream), Map.class);
                    map3.forEach((k, v) -> {
                        var nkBiome = cn.nukkit.level.biome.Biome.getBiome(Double
                                .valueOf(v.get("bedrock_id").toString())
                                .intValue());
                        BIOMES_MAPPING.put(
                                nkBiome, new BiomeType(k)
                        );
                    });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static class HashItem {

        private final Item item;

        private HashItem(Item item) {
            this.item = item;
        }

        public static HashItem of(Item item) {
            return new HashItem(item);
        }

        public Item getItem() {
            return item;
        }

        @Override
        public int hashCode() {
            return this.item.getNamespaceId().hashCode() + this.item.getDamage();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof HashItem hashItem) {
                return this.equals(hashItem);
            } else {
                return false;
            }
        }

        public boolean equals(final HashItem obj) {
            return this.item.getNamespaceId().equals(obj.item.getNamespaceId()) && this.item.getDamage() == obj.item.getDamage();
        }

    }

}
