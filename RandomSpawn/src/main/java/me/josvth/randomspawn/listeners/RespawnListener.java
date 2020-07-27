package me.josvth.randomspawn.listeners;

import java.util.List;
import java.util.Random;

import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.events.NewPlayerSpawn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class RespawnListener implements Listener{
	
	private final RandomSpawn plugin;
	private final Random rng;
	
	public RespawnListener (RandomSpawn plugin){
		this.plugin = plugin;
		this.rng = new Random();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
		Player player = event.getPlayer();
		String playerName = player.getName();
				
		if (player.hasPermission("RandomSpawn.exclude")){ 																// checks if player should be excluded
			plugin.logDebug(playerName + " is excluded from Random Spawning.");
			return; 
		}
		
		World world = event.getRespawnLocation().getWorld();
		String worldName = world.getName();
		
		List<String> randomSpawnFlags = plugin.yamlHandler.worlds.getStringList(worldName + ".randomspawnon");
		List<String> spawnPointFlags = plugin.yamlHandler.worlds.getStringList(worldName + ".spawnpointson");

		// For bedrespawns, if we're not supposed to overwrite them
		if (event.isBedSpawn() && !randomSpawnFlags.contains("bedrespawn")) {
			// Ensure that the player has a bed set before returning
			if (event.getPlayer().getBedSpawnLocation() != null) {
				plugin.logDebug(playerName + " is spawned at his bed!");
				return;
			}
		}
		
		if (plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns", false) && player.getBedSpawnLocation() != null ){
			event.setRespawnLocation(player.getBedSpawnLocation());
			plugin.logDebug(playerName + " is spawned at his saved spawn.");
			return;
		}
		
		if (spawnPointFlags.contains("newplayer")) {
			// check if player is still "new", if so, respawn using point logic again.
			if (event.getPlayer().getFirstPlayed() + 
					plugin.yamlHandler.worlds.getLong(worldName + ".newplayertime", 0) > System.currentTimeMillis()) {
				plugin.logDebug(playerName + " newplayer respawn using Spawn Points");
				// still a new player, continue.
				List<Location> spawnLocations = plugin.findSpawnPoints(world);
				
				int totalTries = spawnLocations.size();
				for (int i = 0 ; i < totalTries ; i++) {
					int j = rng.nextInt(spawnLocations.size());
					Location newSpawn = spawnLocations.get(j);
					NewPlayerSpawn nps = new NewPlayerSpawn(player, newSpawn );
					plugin.getServer().getPluginManager().callEvent(nps);
					if (nps.isCancelled()) {
						spawnLocations.remove(j);
					} else {
						plugin.sendGround(player, newSpawn);
						event.setRespawnLocation(newSpawn);
						player.setMetadata("lasttimerandomspawned", new FixedMetadataValue(plugin, System.currentTimeMillis()));
						
						if (plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns",false)){
							player.setBedSpawnLocation(newSpawn);
						}

						if (plugin.yamlHandler.config.getString("messages.randomspawned") != null){
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.yamlHandler.config.getString("messages.randomspawned")));
						}
						return;
					}
				}
			}
		}
		
		if (randomSpawnFlags.contains("respawn")){
			plugin.logDebug(playerName + " standard Respawn");
			
			Location spawnLocation = plugin.chooseSpawn(world);
			
			if (spawnLocation == null) {
				plugin.logDebug(playerName + " got unlucky and was not successfully randomspawned. Default behavior will apply");
				return;
			}
			
			plugin.sendGround(player, spawnLocation);
			
			event.setRespawnLocation(spawnLocation);
			
			player.setMetadata("lasttimerandomspawned", new FixedMetadataValue(plugin, System.currentTimeMillis()));
						
			if (plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns",false)){
				player.setBedSpawnLocation(spawnLocation);
			}

			if (plugin.yamlHandler.config.getString("messages.randomspawned") != null){
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.yamlHandler.config.getString("messages.randomspawned")));
			}
		}			
	}
}
