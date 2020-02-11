package dev.civmc.bumhug.hacks

import dev.civmc.bumhug.Hack
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class DeathMessages: Hack(), Listener {
	override val configName = "deathMessages"
	override val prettyName = "Death Messages"
	
	private val sendPlayerCoordsOnDeath = config.getBoolean("sendPlayerCoordsOnDeath")
	private val makeDeathMessagesRed = config.getBoolean("redDeathMessages")
	private val deathAnnounce = config.getBoolean("announceDeaths")
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player = event.entity
		if (sendPlayerCoordsOnDeath) {
			val loc = player.location
			player.sendMessage("" + ChatColor.RED + "You died at %d, %d, %d!".format(loc.blockX, loc.blockY, loc.blockZ))
		}
		if (deathAnnounce) {
			if (makeDeathMessagesRed) {
				event.deathMessage = "" + ChatColor.RED + event.deathMessage
			}
		} else {
			event.deathMessage = null
		}
	}
}