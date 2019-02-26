package es.enolgor.eltermc.plugin.features;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import es.enolgor.eltermc.plugin.EltermcAnnotations.Feature;
import es.enolgor.eltermc.plugin.EltermcAnnotations.Inject;

@Feature("levelPenaltyOnDeath")
public class LevelPenaltyOnDeathFeature implements Listener{
	
	@Inject JavaPlugin plugin;
	
	@Feature.onEnable
	public void enable() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Feature.onDisable
	public void disable() {
		PlayerDeathEvent.getHandlerList().unregister(this);
	}
	
	@EventHandler(priority=EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        event.getEntity().setLevel(0);
    }

}
