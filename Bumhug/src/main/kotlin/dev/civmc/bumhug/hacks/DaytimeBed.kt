package dev.civmc.bumhug.hacks

import dev.civmc.bumhug.Hack
import dev.civmc.bumhug.util.REEEEEEEEEE
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerInteractEvent

public class DaytimeBed: Hack(), Listener {
	override val configName = "daytimeBed"
	override val prettyName = "Daytime Bed"
	
	private val message = config.getString("message")
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	fun bedRClickToSetSpawn(event: PlayerInteractEvent) {
		val clickedBlock = event.clickedBlock ?: return
		if (event.action != Action.RIGHT_CLICK_BLOCK || !REEEEEEEEEE.isBed(clickedBlock.type)) {
			return;
		}

		event.player.setBedSpawnLocation(clickedBlock.location, false);
		event.player.sendTitle("", message, 10, 70, 20);
	}
}