package es.enolgor.eltermc.plugin.features;

/*
 * Copyright (c) 2015. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;


public class Util2 {

    public static void initialize() {
    }

    public static String serializeInventory(ItemStack [] is) {
        JsonConfiguration json = new JsonConfiguration();
        json.set("size", is.length);
        int idx = 0;
        HashMap<String, ItemStack> items = new HashMap<>();
        for (ItemStack item : is) {
            int i = idx++;
            if (item == null) {
                continue;
            }
            items.put("" + i, item);
        }
        json.createSection("items", items);
        return json.saveToString();
    }
    public static String dumpItem(ItemStack itemStack) {
        JsonConfiguration json = new JsonConfiguration();
        json.set("item", itemStack);
        return json.saveToString();
    }

    public static ItemStack [] deserializeInventory(String jsons) throws InvalidConfigurationException {
        JsonConfiguration json = new JsonConfiguration();
        json.loadFromString(jsons);

        int size = json.getInt("size");
        
        ItemStack [] is = new ItemStack[size];
        Map<String, Object> items = json.getConfigurationSection("items").getValues(false);
        for (Map.Entry<String, Object> item : items.entrySet()) {
            ItemStack itemstack = (ItemStack) item.getValue();
            int idx = Integer.parseInt(item.getKey());
            is[idx] = itemstack;
        }
        return is;
    }
}

