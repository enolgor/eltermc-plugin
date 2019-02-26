package es.enolgor.eltermc.plugin.features;

import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import es.enolgor.eltermc.plugin.EltermcAnnotations.Feature;
import es.enolgor.eltermc.plugin.EltermcAnnotations.Inject;
import es.enolgor.eltermc.plugin.utils.AES256;

@Feature("levelBooks")
public class LevelBooks implements Listener{
	
	private static String pwd = "6hK5s4wi7Rfdw6";
	private static String bookTitle = "Knowledge Book";
	
	@Inject JavaPlugin plugin;
	@Inject ConfigurationSection config;
	
	@Feature.onEnable
	public void enable() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Feature.onDisable
	public void disable() {
		PlayerInteractEvent.getHandlerList().unregister(this);
	}
	
	@Feature.onCommand
	public boolean onCommand(CommandSender sender, String [] args) {
		Player player;
		if(sender instanceof BlockCommandSender) {
			BlockCommandSender bcs = (BlockCommandSender) sender;
			player = bcs.getBlock().getLocation().getNearbyPlayers(5).stream().sorted(new Comparator<Player>() {
				@Override
				public int compare(Player o1, Player o2) {
					Double d1 = bcs.getBlock().getLocation().distanceSquared(o1.getLocation());
					Double d2 = bcs.getBlock().getLocation().distanceSquared(o2.getLocation());
					return d1.compareTo(d2);
				}
			}).findFirst().orElseGet(() -> null);
		} else {
			player = plugin.getServer().getPlayer(args[0]);
		}
		if(player == null) return true;
		try {
			createBook(player);
		}catch(Exception ex) {
			player.sendMessage(ChatColor.RED + ex.getMessage());
		}
		return true;
	}
	
	@EventHandler(priority=EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		if(!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) return;
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		if(item.getType() != Material.WRITTEN_BOOK) return;
        BookMeta meta = (BookMeta) item.getItemMeta();
		if(!meta.getTitle().equals(LevelBooks.bookTitle)) return;
        try {
        	KnowledgeBook kb = KnowledgeBook.fromItemStack(player.getInventory().getItemInMainHand());
        	useBook(player, kb);
		} catch (IncorrectBookException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
		}
        event.setCancelled(true);
    }
	
	public void createBook(Player player) throws BookCreationException{
		int targetSlot = player.getInventory().firstEmpty();
		if(targetSlot == -1) throw new BookCreationException("Your inventory is full");
		KnowledgeBook knowledgeBook = KnowledgeBook.create(player);
		player.getInventory().setItem(targetSlot, knowledgeBook.getItemStack());
		player.setLevel(0);
		player.setExp(0);
	}
	
	public void useBook(Player player, KnowledgeBook kb) throws IncorrectBookException{
		if(config.getBoolean("ownerCheck") && !kb.uuid.equals(player.getUniqueId())) {
			throw new IncorrectBookException("You are not allowed to use this book");
    	} else {
    		player.sendMessage(ChatColor.GREEN + "You get " + kb.exp + " experience from the knowledge book!");
    		player.giveExp(kb.exp, false);
			player.getInventory().getItemInMainHand().setAmount(0);
    	}
	}
	
	@SuppressWarnings("serial")
	public static class BookCreationException extends Exception {
		public BookCreationException(String message) {
			super(message);
		}
	}
	
	@SuppressWarnings("serial")
	public static class IncorrectBookException extends Exception {
		public IncorrectBookException(String message) {
			super(message);
		}
	}
	
	public static class KnowledgeBook {
		
		private ItemStack itemStack;
		private int exp;
		private UUID uuid;
		
		public ItemStack getItemStack() {
			return itemStack;
		}
		
		public static KnowledgeBook create(Player player) throws BookCreationException {
			KnowledgeBook kb = new KnowledgeBook();
			kb.itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);
			BookMeta meta = (BookMeta) kb.itemStack.getItemMeta();
			meta.setAuthor(player.getName());
			meta.setTitle(LevelBooks.bookTitle);
			kb.exp = getPlayerExp(player);
			if(kb.exp == 0) throw new BookCreationException("You need some exp to create a book!");
			kb.uuid = player.getUniqueId();
			meta.setLore(Arrays.asList(
				String.format("%s XP", kb.exp),
				String.format("From 0 to %s", player.getLevel())
			));
			try {
				String content = String.format("%s;%s;salt-%s", player.getUniqueId().toString(), kb.exp, System.currentTimeMillis());
				meta.addPage(AES256.encrypt(content, LevelBooks.pwd));
			} catch (Exception e) {
				e.printStackTrace();
				throw new BookCreationException("Server encountered an error creating book");
			}
			kb.itemStack.setItemMeta(meta);
			return kb;
		}
		
		public static KnowledgeBook fromItemStack(ItemStack itemStack) throws IncorrectBookException{
			if(itemStack.getType() != Material.WRITTEN_BOOK) throw new IncorrectBookException("Not a book");
			BookMeta meta = (BookMeta) itemStack.getItemMeta();
			if(!meta.getTitle().equals(LevelBooks.bookTitle)) throw new IncorrectBookException("Not a knowledge book");
			String content = meta.getPage(1);
			KnowledgeBook kb = new KnowledgeBook();
			String [] parts;
			try {
				parts = AES256.decrypt(content, LevelBooks.pwd).split(";");
			} catch (Exception e) {
				e.printStackTrace();
				throw new IncorrectBookException("Server encountered an error creating book");
			}
			try {
				kb.uuid = UUID.fromString(parts[0]);
				kb.exp = Integer.parseInt(parts[1]);
			}catch(Exception ex) {
				throw new IncorrectBookException("Book format is incorrect");
			}
			kb.itemStack = itemStack;
			return kb;
		}
		
		private static int getExpToLevelUp(int level){
	        if(level <= 15){
	            return 2*level+7;
	        } else if(level <= 30){
	            return 5*level-38;
	        } else {
	            return 9*level-158;
	        }
	    }
	  
	    private static int getExpAtLevel(int level){
	        if(level <= 16){
	            return (int) (Math.pow(level,2) + 6*level);
	        } else if(level <= 31){
	            return (int) (2.5*Math.pow(level,2) - 40.5*level + 360.0);
	        } else {
	            return (int) (4.5*Math.pow(level,2) - 162.5*level + 2220.0);
	        }
	    }
	  
	    private static int getPlayerExp(Player player){
	        int exp = 0;
	        int level = player.getLevel();
	        exp += getExpAtLevel(level);
	        exp += Math.round(getExpToLevelUp(level) * player.getExp());
	        return exp;
	    }
		
	}

}
