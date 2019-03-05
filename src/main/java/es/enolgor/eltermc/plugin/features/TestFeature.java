package es.enolgor.eltermc.plugin.features;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import es.enolgor.eltermc.plugin.EltermcAnnotations.Feature;
import es.enolgor.eltermc.plugin.EltermcAnnotations.Inject;

@Feature("testFeature")
public class TestFeature implements Listener{
	
	@Inject JavaPlugin plugin;
	@Inject ConfigurationSection section;
	
	@Feature.onEnable
	public void enable() {
		// plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Feature.onDisable
	public void disable() {
		// PlayerDeathEvent.getHandlerList().unregister(this);
	}
	
	@Feature.onCommand
	public boolean onCommand(CommandSender sender, String [] args) {
		switch(args[0]) {
		case "get":
			try {
				ItemStack [] inventoryItems = plugin.getServer().getPlayer("elterpek").getInventory().getContents();
				String jsonInventoryItems = Util2.serializeInventory(inventoryItems);
				String b64InventoryItems = Base64.getEncoder().encodeToString(jsonInventoryItems.getBytes("utf8"));
				System.out.println(b64InventoryItems);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case "set":
			try {
				String b64InventoryItems = args[1];
				String jsonInventoryItems = new String(Base64.getDecoder().decode(b64InventoryItems.getBytes(StandardCharsets.ISO_8859_1)), "utf8");
				ItemStack [] inventoryItems = Util2.deserializeInventory(jsonInventoryItems);
				plugin.getServer().getPlayer("elterpek").getInventory().setContents(inventoryItems);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
	
		return true;
	}
}
