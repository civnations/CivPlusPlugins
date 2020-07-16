package dev.civmc.bumhug.hacks

import dev.civmc.bumhug.Hack
import dev.civmc.bumhug.util.tryToTeleportVertically
import org.bukkit.GameMode
import org.bukkit.Tag
import org.bukkit.World.Environment
import org.bukkit.block.Biome
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.InventoryHolder

class GameFixes: Hack(), Listener {
	override val configName = "gameFixes"
	override val prettyName = "Game Fixes"
	
	private val preventStorageTeleport = config.getBoolean("preventStorageTeleport")
	private val preventBedBombing = config.getBoolean("preventBedBombing")
	private val preventFallingThroughBedrock = config.getBoolean("preventFallingThroughBedrock")

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onStorageTeleport(event: EntityTeleportEvent) {
		if (event.entity is InventoryHolder && preventStorageTeleport) {
			event.isCancelled = true
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onStoragePortal(event: EntityPortalEvent) {
		if (event.entity is InventoryHolder && preventStorageTeleport) {
			event.isCancelled = true
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	fun onBedPlace(event: BlockPlaceEvent) {
		if (!preventBedBombing || !Tag.BEDS.isTagged(event.block.type)) {
			return
		}
		val world = event.block.location.world ?: return
		val env = world.environment
		val biome = event.block.biome
		if (env == Environment.NETHER || env == Environment.THE_END || biome == Biome.NETHER) {
			event.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	fun onPlayerFallThroughBedrock(event: PlayerMoveEvent) {
		if (preventFallingThroughBedrock) {
			val to = event.to ?: return
			if (to.y >= 0)
				return

			if (event.from.y <= -3) {
				// prevent excessive calls to tryToTeleportVertically
				// this uses from rather than to because in lag to may jump from greater than 0 to less than negative 3,
				// while from will never jump like that
				return
			}

			if (event.player.gameMode != GameMode.SURVIVAL)
				return

			tryToTeleportVertically(event.player, to, "falling into the void")
		}
	}
}