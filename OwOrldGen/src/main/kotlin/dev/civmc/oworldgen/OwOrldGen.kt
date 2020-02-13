package dev.civmc.oworldgen

import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin

class OWOrldGen: JavaPlugin() {
	companion object {
		private var instanceStorage: OWOrldGen? = null

		internal val instance: OWOrldGen
			get() = instanceStorage!!
	}
	
	override fun onEnable() {
		saveDefaultConfig()
		instanceStorage = this
	}

	override fun getDefaultWorldGenerator(worldName: String, id: String): ChunkGenerator {
        return WorldGenerator(config.getConfigurationSection(worldName))
	}
}
