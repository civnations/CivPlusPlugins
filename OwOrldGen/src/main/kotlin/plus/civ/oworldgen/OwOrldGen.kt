package plus.civ.oworldgen

import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class OwOrldGen: JavaPlugin() {
	companion object {
		private var instanceStorage: OwOrldGen? = null
		internal val instance: OwOrldGen
			get() = instanceStorage!!

		private var configManagerStorage: ConfigManager? = null
		internal val configManager: ConfigManager
			get() = configManagerStorage!!
	}
	
	override fun onEnable() {
		saveDefaultConfig()
		instanceStorage = this
		configManagerStorage = ConfigManager(config)
	}

	override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return WorldGenerator()
	}
}
