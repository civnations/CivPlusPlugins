package plus.civ.vorpalsword.tracking

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import plus.civ.vorpalsword.database.PrisonedPlayer
import plus.civ.vorpalsword.database.PrisonedPlayer.Companion.isPrisoned

object LastSeenUpdate: Listener {
	@EventHandler
	fun onJoin(event: PlayerJoinEvent) {
		if (!event.player.isPrisoned()) {
			return
		}

		val prisonedPlayer = PrisonedPlayer.prisonedPlayers[event.player]!!
		prisonedPlayer.lastSeen = System.currentTimeMillis() / 1000
	}
}
