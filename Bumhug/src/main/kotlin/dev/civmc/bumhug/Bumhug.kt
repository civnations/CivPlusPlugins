package dev.civmc.bumhug

import com.google.common.reflect.ClassPath
import org.bukkit.command.CommandExecutor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import kotlin.reflect.KAnnotatedElement

/**
 * Much of the code in this plugin was adapted from ProgrammerDan's code from SimpleAdminHacks
 * and the work of the many authors of Humbug over the years
 */
class Bumhug: JavaPlugin() {
	companion object {
		private var instanceStorage: Bumhug? = null

		internal val instance: Bumhug
			get() = instanceStorage!!

		var hacks = HashMap<String, Hack>()
	}
	
	override fun onEnable() {
		instanceStorage = this
		this.saveDefaultConfig()
		loadHacks()
	}
	
	private fun loadHacks() {
		val samplersPath = ClassPath.from(this.getClassLoader());
		hacks@ for (clsInfo in samplersPath.getTopLevelClasses("dev.civmc.bumhug.hacks")) {
			val clazz = clsInfo.load()
			if (clazz != null && Hack::class.java.isAssignableFrom(clazz)) {
				logger.log(Level.INFO, "Found hack " + clazz.typeName)
				
				val hack = clazz.getDeclaredConstructor().newInstance() as Hack
				if (hack.enabled) {
					// if the hack has the Depend annotation, make sure all dependencies are loaded
					val annotations = (clazz.kotlin as KAnnotatedElement).annotations
					for (annotation in annotations) {
						if (annotation.annotationClass == Depend::class) {
							val depends = annotation as Depend
							for (dependency in depends.dependencies) {
								if (this.server.pluginManager.getPlugin(dependency) == null) {
									logger.log(Level.WARNING, "Couldn't load hack " + clazz.typeName + ". Missing dependency " + dependency)
									continue@hacks
								}
							}
						}
					}
					
					hacks[hack.configName] = hack
					if (Listener::class.java.isAssignableFrom(hack::class.java)) {
						this.server.pluginManager.registerEvents(hack as Listener, this)
					}

                    if (CommandExecutor::class.java.isAssignableFrom(hack::class.java)) {
						val commandName = hack.commandName ?: continue
						val command = getCommand(commandName) ?: continue
						command.setExecutor(hack as CommandExecutor)
                    }

					hack.onEnable()

					logger.log(Level.INFO, "Loaded hack " + hack.prettyName)
				}
			}
		}
	}
	
	fun broadcastToPerm(perm: String, message: String) {
		for (player in this.server.onlinePlayers) {
			if (player.hasPermission("bumhug.$perm")) {
				player.sendMessage(message)
			}
		}
	}
}