package plus.civ.oworldgen

import nl.rutgerkok.worldgeneratorapi.decoration.DecorationType
import nl.rutgerkok.worldgeneratorapi.event.WorldGeneratorInitEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class OwOrldGen: JavaPlugin(), Listener {
	companion object {
		private var instanceStorage: OwOrldGen? = null
		internal val instance: OwOrldGen
			get() = instanceStorage!!
	}

	override fun onEnable() {
		instanceStorage = this
		server.pluginManager.registerEvents(this, this)
	}

	@EventHandler
	fun onWorldGeneratorInit(event: WorldGeneratorInitEvent) {
		event.worldGenerator.biomeGenerator = RadialBiomeGenerator()
		event.worldGenerator.worldDecorator.withoutDefaultDecorations(DecorationType.UNDERGROUND_ORES)
		event.worldGenerator.worldDecorator.withoutDefaultDecorations(DecorationType.SURFACE_STRUCTURES)
		event.worldGenerator.worldDecorator.withoutDefaultDecorations(DecorationType.UNDERGROUND_STRUCTURES)
		event.worldGenerator.worldDecorator.withoutDefaultDecorations(DecorationType.UNDERGROUND_DECORATION)
	}
}
