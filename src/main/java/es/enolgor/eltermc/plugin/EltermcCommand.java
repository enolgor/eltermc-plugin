package es.enolgor.eltermc.plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import es.enolgor.eltermc.plugin.FeatureLoader.NoSuchFeatureException;

public class EltermcCommand implements CommandExecutor, TabCompleter{

	private final FeatureLoader featureLoader;
	
	public EltermcCommand(FeatureLoader featureLoader) {
		this.featureLoader = featureLoader;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 1) return false;
		switch(args[0]) {
		case "feature": return processFeatureSubCommand(sender, Arrays.copyOfRange(args, 1, args.length));
		default:
			return false;
		}
	}
	
	private boolean processFeatureSubCommand(CommandSender sender, String [] args) {
		if(args.length == 0) return false;
		if(args.length == 1) {
			switch(args[0]) {
			case "list":
				sender.sendMessage(Stream.of(featureLoader.loadedFeatures())
					.map(feature -> {
						try {
							return String.format("%s: %s", feature, featureLoader.isEnabled(feature) ? "enabled" : "disabled");
						} catch (NoSuchFeatureException e) {
							e.printStackTrace();
							return null;
						}
					})
					.filter(s -> s != null)
					.collect(Collectors.toSet()).toArray(new String[] {})
				);
				return true;
			default:
				return false;
			}
		}
		if(args.length == 2) {
			try {
				switch(args[0]) {
				case "enable": {
					featureLoader.enable(args[1]);
					return true;
				}
				case "disable": {
					featureLoader.disable(args[1]);
					return true;
				}
				case "status": {
					sender.sendMessage(String.format("%s is %s", args[1], featureLoader.isEnabled(args[1]) ? "enabled" : "disabled"));
					return true;
				}
				case "command": {
					try {
						return featureLoader.command(args[1], sender, Arrays.copyOfRange(args, 2, args.length));
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}
				default:
					return false;
				}
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		if(args.length > 2) {
			if(args[0].equals("command")) {
				try {
					return featureLoader.command(args[1], sender, Arrays.copyOfRange(args, 2, args.length));
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(sender instanceof Player) {
			switch(args.length) {
			case 1: return Arrays.asList("feature");
			case 2: return Arrays.asList("enable", "disable", "list", "status", "command");
			case 3: 
				if(args[1].equals("list")) return null;
				return Arrays.asList(featureLoader.loadedFeatures());
			default:
				return null;
			}
		}
		return null;
	}

}
