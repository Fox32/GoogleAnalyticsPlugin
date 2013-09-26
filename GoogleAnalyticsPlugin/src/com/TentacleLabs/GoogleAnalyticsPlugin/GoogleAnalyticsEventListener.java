package com.TentacleLabs.GoogleAnalyticsPlugin;

import java.net.InetAddress;
import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * The entity listener for tracking events
 * 
 * @author Oliver
 *
 */
public class GoogleAnalyticsEventListener implements Listener {
	private final GoogleAnalyticsPlugin plugin;

	private final boolean enableEventDeath;
	private final boolean enableEventKill;
	private final boolean enableEventTryLogin;
	private final boolean enableEventLogin;
	private final boolean enableEventQuit;
	private final boolean enableEventEnter;
	private final boolean enableEventRespawn;
	private final boolean enableEventKicked;
	private final boolean enableEventEnchantItem;
	private final boolean enableEventTame;
	private final boolean enableEventGameModeChange;
	private final boolean enableEventLevelUp;

	private final HashMap<String, Long> playerJoinedTime = new HashMap<String, Long>();
	
	
	public GoogleAnalyticsEventListener(GoogleAnalyticsPlugin instance) {
	    this.plugin = instance;
	    
	    // Load config
	    enableEventDeath = plugin.getConfig().getBoolean("track_events.death", true);
	    enableEventKill = plugin.getConfig().getBoolean("track_events.kill", true);
	    enableEventTryLogin = plugin.getConfig().getBoolean("track_events.trylogin", true);
	    enableEventLogin = plugin.getConfig().getBoolean("track_events.login", true);
	    enableEventQuit = plugin.getConfig().getBoolean("track_events.quit", true);
	    enableEventEnter = plugin.getConfig().getBoolean("track_events.enter", true);
	    enableEventRespawn = plugin.getConfig().getBoolean("track_events.respawn", true);
	    enableEventKicked = plugin.getConfig().getBoolean("track_events.kicked", true);
	    enableEventEnchantItem = plugin.getConfig().getBoolean("track_events.enchantitem", true);
	    enableEventTame = plugin.getConfig().getBoolean("track_events.tame", true);
	    enableEventGameModeChange = plugin.getConfig().getBoolean("track_events.gamemodechange", true);
	    enableEventLevelUp = plugin.getConfig().getBoolean("track_events.levelup", true);
	}


	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event){
		if(enableEventTryLogin) {
			try {
				Player player = event.getPlayer();

				if(!player.hasPermission("googleanalyticsplugin.ignore")) {
					plugin.getTracker().Track(getClientName(plugin, player), getClientId(player), getClientIP(event.getAddress()), player.getName(), "TryLogin", player.isWhitelisted() ? "Whitelisted" : "Not whitelisted");
				}
			}
			catch(Exception e) {
		  		plugin.getLogger().warning("Event Listener Error: " + e.getMessage());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event){
		if(enableEventLogin) {
			try {
				Player player = event.getPlayer();		

				if(!player.hasPermission("googleanalyticsplugin.ignore")) {
					playerJoinedTime.put(player.getName(), System.currentTimeMillis());
					
					plugin.getTracker().Track(getClientName(plugin, player), getClientId(player), getClientIP(player.getAddress().getAddress()), player.getName(), "Login", player.isOp() ? "Operator" : "Player");   
				}
			}
			catch(Exception e) {
				plugin.getLogger().warning("Event Listener Error: " + e.getMessage());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event){
		if(enableEventQuit) {
			try {
				Player player = event.getPlayer();		
				
				if(!player.hasPermission("googleanalyticsplugin.ignore")) {
					Long joinTime = playerJoinedTime.get(player.getName());
					String playTime = "Playtime Unkown";
					
					if(joinTime != null) {
						long time = (System.currentTimeMillis() - joinTime) / 1000;
						
						if(time / 60 / 60 >= 5) {
							playTime = "Played more than 5 hours";
						}
						else if(time / 60 / 60 >= 4) {
							playTime = "Played 4 hours";
						}
						else if(time / 60 / 60 >= 3) {
							playTime = "Played 3 hours";
						}
						else if(time / 60 / 60 >= 2) {
							playTime = "Played 2 hours";
						}
						else if(time / 60 / 60 >= 1) {
							playTime = "Played 1 hour";
						}
						else if(time / 60 <= 30 && time / 60 > 5) {
							playTime = "Played less than 30 minutes";
						}
						else {
							playTime = "Played less than 5 minutes";
						}						
					}							

					plugin.getTracker().Track(getClientName(plugin, player), getClientId(player), getClientIP(player.getAddress().getAddress()), player.getName(), "Quit", playTime);
				}
			}
			catch(Exception e) {
				plugin.getLogger().warning("Event Listener Error: " + e.getMessage());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event){
		if(enableEventEnter) {
			try {
				Player player = event.getPlayer();
			
				if(!player.hasPermission("googleanalyticsplugin.ignore")) {
					String worldName = player.getLocation().getWorld().getName();
					
					plugin.getTracker().TrackAction(getClientName(plugin, player), getClientId(player), getClientIP(player.getAddress().getAddress()), player.getName(), "Enter", worldName);
				}
			}
			catch(Exception e) {
				plugin.getLogger().warning("Event Listener Error: " + e.getMessage());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event){
		if(enableEventRespawn) {
			try {
				Player player = event.getPlayer();
				
				if(!player.hasPermission("googleanalyticsplugin.ignore")) {
					plugin.getTracker().TrackAction(getClientName(plugin, player), getClientId(player), getClientIP(player.getAddress().getAddress()), player.getName(), "Respawn", event.isBedSpawn() ? "At bed" : "At spawn");
				}
			}
			catch(Exception e) {
				plugin.getLogger().warning("Event Listener Error: " + e.getMessage());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event){
		if(enableEventKicked) {
			try {
				Player player = event.getPlayer();

				if(!player.hasPermission("googleanalyticsplugin.ignore")) {
					plugin.getTracker().TrackAction(getClientName(plugin, player), getClientId(player), getClientIP(player.getAddress().getAddress()), player.getName(), "Kicked", event.getReason());
				}
			}
			catch(Exception e) {
				plugin.getLogger().warning("Event Listener Error: " + e.getMessage());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event){
		Entity entity = event.getEntity();
		
		// Died Event
		if(entity instanceof Player && enableEventDeath) {
			try {
				Player player = (Player)entity;

				if(!player.hasPermission("googleanalyticsplugin.ignore")) {
					plugin.getTracker().TrackAction(getClientName(plugin, player), getClientId(player), getClientIP(player.getAddress().getAddress()), player.getName(), "Died", entity.getLastDamageCause() != null ? getDamageName(entity.getLastDamageCause().getCause()) : "Unkown");
				}
			}
			catch(Exception e) {
		  		plugin.getLogger().warning("Event Listener Error: " + e.getMessage());
			}
		}
		
		// Kill Event
		if(entity instanceof LivingEntity) {
			try {
				Player player = ((LivingEntity) entity).getKiller();
							
				if(player != null && !player.hasPermission("googleanalyticsplugin.ignore") && enableEventKill) {
					plugin.getTracker().TrackAction(getClientName(plugin, player), getClientId(player), getClientIP(player.getAddress().getAddress()), player.getName(), "Kill", getEntityName(entity));
				}	
			}
			catch(Exception e) {
		  		plugin.getLogger().warning("Event Listener Error: " + e.getMessage());
			}		
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEnchantItem(EnchantItemEvent event){
		if(enableEventEnchantItem) {
			try {
				Player player = event.getEnchanter();
				
				if(!player.hasPermission("googleanalyticsplugin.ignore")) {
					plugin.getTracker().TrackAction(getClientName(plugin, player), getClientId(player), getClientIP(player.getAddress().getAddress()), player.getName(), "Enchant Item", getItemSummary(event));
				}
			}
			catch(Exception e) {
				plugin.getLogger().warning("Event Listener Error: " + e.getMessage());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityTame(EntityTameEvent event){
		if(enableEventTame) {
			try {
				Player player = (Player) event.getOwner();
				
				if(!player.hasPermission("googleanalyticsplugin.ignore")) {
					Entity entity = event.getEntity();

					plugin.getTracker().TrackAction(getClientName(plugin, player), getClientId(player), getClientIP(player.getAddress().getAddress()), player.getName(), "Tamed", getEntityName(entity));
				}
			}
			catch(Exception e) {
				plugin.getLogger().warning("Event Listener Error: " + e.getMessage());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event){
		if(enableEventGameModeChange) {
			try {
				Player player = (Player) event.getPlayer();

				if(!player.hasPermission("googleanalyticsplugin.ignore")) {
					plugin.getTracker().TrackAction(getClientName(plugin, player), getClientId(player), getClientIP(player.getAddress().getAddress()), player.getName(), "Game Mode Change", "Game Mode " + getGamemode(event.getNewGameMode()));
				}
			}
			catch(Exception e) {
				plugin.getLogger().warning("Event Listener Error: " + e.getMessage());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLevelChange(PlayerLevelChangeEvent event){
		if(enableEventLevelUp) {
			try {
				Player player = (Player) event.getPlayer();

				if(!player.hasPermission("googleanalyticsplugin.ignore")) {
					if(event.getOldLevel() < event.getNewLevel()) {
						plugin.getTracker().TrackAction(getClientName(plugin, player), getClientId(player), getClientIP(player.getAddress().getAddress()), player.getName(), "Levelup", String.format("Level %3s",  "" + player.getLevel()));
					}
				}				
			}
			catch(Exception e) {
				plugin.getLogger().warning("Event Listener Error: " + e.getMessage());
			}
		}
	}
	
	
	private static String getEntityName(Entity entity) {
		String victimName = "";
		
		if(entity instanceof Player) {
			Player victim = (Player)entity;
			
			victimName = victim.getName();
		}
		else {
			String className = entity.getClass().getName();
			
			victimName = className.substring(className.indexOf(".Craft") + ".Craft".length());
		}
		
		return victimName;
	}

	private static String getClientName(GoogleAnalyticsPlugin plugin, Player player) {
	    boolean usesPluginChannels = player.getListeningPluginChannels().size() != 0;
	    String serverVersion = plugin.getServer().getVersion().substring("git-Bukkit-".length());	    
	    String clientVersion = serverVersion.substring(0, serverVersion.indexOf('-'));
	    String clientName = "Minecraft";
	    
	    // Check for other clients here...
	    
		return clientName + " " + clientVersion + (usesPluginChannels ? " [Supports Plugin Channels]" : "");
	}

	private static String getDamageName(DamageCause cause) {
		switch (cause) {
		case BLOCK_EXPLOSION:
			return "Died from an explosion";
		case CONTACT:
            return "Was squished";
		case CUSTOM:
            return "Strangely died";
		case DROWNING:
            return "Drowned to death";
		case ENTITY_ATTACK:
            return "Was brutally attacked";
		case ENTITY_EXPLOSION :
            return "Got nerfed by a creeper";
		case FALL:
            return "Fell to there death";
		case FIRE:
            return "Was burnt to death";
		case FIRE_TICK:
			return "Got smoked";
		case LAVA:
			return "Went swimming in magma";			
		case LIGHTNING:
			return "Was hit by a lightning";	
		case MAGIC:
			return "Was killed by magic";
		case MELTING:
			return "Melted away";
		case POISON:
			return "Was poisoned";
		case PROJECTILE:
			return "Was shoot";
		case STARVATION:
			return "Starved";
		case SUFFOCATION:
			return "Suffocated";
		case SUICIDE:
			return "Committed suicide";
		case VOID:
			return "Fell into the void";			
		default:
			return cause.toString();
		}
	}

	private static String getItemSummary(EnchantItemEvent event) {
		String itemSummary = event.getItem().getType().name() + " with";
		
		for(Enchantment enchantment : event.getEnchantsToAdd().keySet()) {
			itemSummary += " " + enchantment.getName() + " [" + event.getEnchantsToAdd().get(enchantment) + "]";
		}

		return itemSummary;
	}

	private static String getGamemode(GameMode newGameMode) {
		switch (newGameMode) {
		case CREATIVE:
			return "Creative";
		case SURVIVAL:
			return "Survival";
		case ADVENTURE:
			return "Adventure";
		default:
			return newGameMode.toString();
		}
	}
	
	public static String getClientIP(InetAddress inetAddress) {
		return inetAddress != null ? inetAddress.toString().substring(1) : "0.0.0.0";
	}
	
	public static String getClientId(Player player) {
		return player.getName();
	}
}
