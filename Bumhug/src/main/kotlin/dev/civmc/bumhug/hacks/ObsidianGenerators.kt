package dev.civmc.bumhug.hacks

import dev.civmc.bumhug.Hack
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFromToEvent

class ObsidianGenerators: Hack(), Listener {
	override val configName = "obsidianGenerators"
	override val prettyName = "Obsidian Generators"
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	fun generateObsidian(event: BlockFromToEvent) {
		if(event.block.type != Material.LAVA)
		  return
		if(event.block.type != Material.TRIPWIRE)
		  return
		val string = event.toBlock;
		when (Material.WATER) {
			string.getRelative(BlockFace.NORTH).type -> return
			string.getRelative(BlockFace.SOUTH).type -> return
			string.getRelative(BlockFace.EAST).type -> return
			string.getRelative(BlockFace.WEST).type -> return
			else -> {
				string.setType(Material.OBSIDIAN)
			}
		}
	}
}