package es.enolgor.eltermc.plugin.features;

/*
 * Copyright (c) 2015. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;


public class Util {

    public static void initialize() {
    }

    public static String serializeInventory(Inventory inventory) {
        JsonConfiguration json = new JsonConfiguration();
        json.set("size", inventory.getSize());
        int idx = 0;
        HashMap<String, ItemStack> items = new HashMap<>();
        for (ItemStack item : inventory.getContents()) {
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

    public static Inventory serializeInventory(String jsons) throws InvalidConfigurationException {
        return deserializeInventory(jsons, null);
    }

    public static Inventory deserializeInventory(String jsons, String title) throws InvalidConfigurationException {
        JsonConfiguration json = new JsonConfiguration();
        json.loadFromString(jsons);

        int size = json.getInt("size", 54);
        if (title == null) {
            title = json.getString("name");
        }

        Inventory inventory = Bukkit.createInventory(null, size, title);
        Map<String, Object> items = json.getConfigurationSection("items").getValues(false);
        for (Map.Entry<String, Object> item : items.entrySet()) {
            ItemStack itemstack = (ItemStack) item.getValue();
            int idx = Integer.parseInt(item.getKey());
            inventory.setItem(idx, itemstack);
        }
        return inventory;
    }
}

