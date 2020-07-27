package plus.civ.vorpalsword.restriction

import me.josvth.randomspawn.RandomSpawn
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRespawnEvent
import plus.civ.vorpalsword.VorpalSword
import plus.civ.vorpalsword.database.PrisonedPlayer.Companion.isPrisoned

/**
 * Teleports the player into the end and keeps them there if they are imprisoned.
 */
object PlayerEndRestriction: Listener {
	val randomSpawn = VorpalSword.instance.server.pluginManager.getPlugin("RandomSpawn") as RandomSpawn

	fun sendToEnd(player: Player) {
		if (player.location.world == VorpalSword.configManager.spawnWorld) {
			return
		}

		val spawnPoint = randomSpawn.chooseSpawn(VorpalSword.configManager.spawnWorld)
		player.teleport(spawnPoint)
	}

	@EventHandler
	fun onPlayerSpawn(event: PlayerRespawnEvent) {
		if (!event.player.isPrisoned()) {
			return
		}

		event.respawnLocation = randomSpawn.chooseSpawn(VorpalSword.configManager.spawnWorld)
	}

	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		if (!event.player.isPrisoned()) {
			return
		}

		sendToEnd(event.player)
	}

	// Some random stuff that makes it hard to exploit anything if you end up in the overworld:

	@EventHandler
	fun onPlayerMove(event: PlayerMoveEvent) {
		if (event.to!!.block.location == event.from.block.location) {
			return
		}

		if (!event.player.isPrisoned()) {
			return
		}

		sendToEnd(event.player)
	}

	@EventHandler
	fun playerInteract(event: PlayerInteractEvent) {
		if (!event.player.isPrisoned()) {
			return
		}

		sendToEnd(event.player)
	}
}
