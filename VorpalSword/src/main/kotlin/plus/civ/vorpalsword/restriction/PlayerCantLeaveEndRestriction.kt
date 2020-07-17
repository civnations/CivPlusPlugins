package plus.civ.vorpalsword.restriction

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent
import plus.civ.vorpalsword.database.isPrisoned

/**
 * Prevents the player from entering the end portal if they are imprisoned.
 */
class PlayerCantLeaveEndRestriction: Listener {
	@EventHandler
	fun playerPortal(event: PlayerPortalEvent) {
		if (event.cause != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
			return
		}

		if (!event.player.isPrisoned()) {
			return
		}

		event.isCancelled = true
	}
}
