package es.enolgor.eltermc.plugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class EltermcPlugin extends JavaPlugin{
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		ConfigurationSection featuresConfigSection = getConfig().getConfigurationSection("features");
		FeatureLoader featureLoader = new FeatureLoader(this, featuresConfigSection);
		this.getCommand("eltermc").setExecutor(new EltermcCommand(featureLoader));
		featureLoader.loadFromInputStream(EltermcPlugin.class.getResourceAsStream("/features"));
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Eltermc Plugin disabled!");
	}
}
