package es.enolgor.eltermc.plugin.features.webinventory;

import java.io.UnsupportedEncodingException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import es.enolgor.eltermc.plugin.EltermcAnnotations.Feature;
import es.enolgor.eltermc.plugin.EltermcAnnotations.Inject;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

@Feature("webInventory")
public class WebInventory implements Listener{
	
	@Inject JavaPlugin plugin;
	@Inject ConfigurationSection section;
	
	private Socket socket = null;
	
	private Emitter.Listener onConnect = new Emitter.Listener() {
		
		@Override
		public void call(Object... args) {
			plugin.getLogger().info("Connected to webInventory server");
		}
		
	};
	
	private Emitter.Listener onDisconnect = new Emitter.Listener() {
		
		@Override
		public void call(Object... args) {
			plugin.getLogger().info("Disconnected from webInventory server");
		}
		
	};
	
	private Emitter.Listener onGetInventory = new Emitter.Listener() {
		
		@Override
		public void call(Object... args) {
			try {
				String player = args[0].toString();
				ItemStack[] inventoryItems = plugin.getServer().getPlayer(player).getInventory().getContents();
				socket.emit("get-inventory-response", Util.serializeInventoryB64(inventoryItems));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		
		}
		
	};
	
	@Feature.onEnable
	public void enable() {
		try {
			socket = IO.socket(this.section.getString("server"));
			socket
				.on(Socket.EVENT_CONNECT, onConnect)
				.on(Socket.EVENT_DISCONNECT, onDisconnect)
				.on("get-inventory", onGetInventory);
			plugin.getLogger().info("Connecting to webInventory server...");
			socket.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Feature.onDisable
	public void disable() {
		this.socket.disconnect();
	}
}
