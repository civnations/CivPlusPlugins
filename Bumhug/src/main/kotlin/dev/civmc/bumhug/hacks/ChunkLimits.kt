package dev.civmc.bumhug.hacks

import dev.civmc.bumhug.Hack
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import java.util.*

class ChunkLimits: Hack(), Listener {
	override val configName = "chunkLimits"
	override val prettyName = "Chunk Limits"
	
	private val limits: EnumMap<Material, Int> = EnumMap(org.bukkit.Material::class.java)
	init {
		val materials = config.getConfigurationSection("limits")
		if (materials != null) {
			for (key in materials.getKeys(false)) {
				val mat = Material.getMaterial(key) ?: continue
				val lim = materials.getInt(key)
				limits[mat] = lim
			}
		}
	}
	private val chunkLimitExceededMessage: String = config.getString("message", "&9You've exceeded the chunk limit for that block type!")!!
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	fun onBlockPlace(event: BlockPlaceEvent) {
		if (event.player.hasPermission("bumhug.bypassChunkLimits")) {
			return
		}
		val bl = event.block
		val lim = limits[bl.type] ?: return

		var count = 0
		for (state in bl.chunk.tileEntities) {
			if (bl.type == state.type) {
				if (++count > lim) {
					event.isCancelled = true
					event.player.sendMessage(chunkLimitExceededMessage)
					return
				}
			}
		}
	}
}