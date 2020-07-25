package plus.civ.vorpalsword.tracking

import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import plus.civ.vorpalsword.VorpalSword
import plus.civ.vorpalsword.database.isPrisoned

object FreeManInEndMessage: Listener {
	@EventHandler
	fun onLoginInEnd(event: PlayerJoinEvent) {
		if (event.player.location.world != VorpalSword.configManager.spawnWorld)
			return
		if (event.player.isPrisoned())
			return
		event.player.sendMessage("${ChatColor.GREEN}You are a free man. Use the End Portal to go back to your own world.")
	}
}
