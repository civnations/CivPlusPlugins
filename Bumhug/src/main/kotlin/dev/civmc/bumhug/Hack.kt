package dev.civmc.bumhug

import org.bukkit.configuration.ConfigurationSection

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Depend(vararg val dependencies: String)

abstract class Hack {
	
	abstract val configName: String
	open val prettyName: String = configName
	
	val enabled: Boolean
		get() = Bumhug.instance.config.getBoolean("$configName.enabled")
	protected val config: ConfigurationSection
		get() {
			val ret = Bumhug.instance.config.getConfigurationSection(configName) ?: throw Exception("Config doesn't include section $configName. Check your config.")
			return ret
		}

	open val commandName: String? = null
}