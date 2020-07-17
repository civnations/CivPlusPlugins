package plus.civ.vorpalsword

import org.bukkit.World
import org.bukkit.configuration.Configuration

class ConfigManager(config: Configuration) {
	val dbUsername: String
	val dbPassword: String
	val dbHost: String
	val dbPort: Int
	val dbName: String

	/**
	 * The world imprisoned players spawn in, according to the RandomSpawn config.
	 */
	val spawnWorld: World

	init {
		val database = config.getConfigurationSection("database")!!
		dbUsername = database.getString("username")!!
		dbPassword = database.getString("password")!!
		dbHost = database.getString("host")!!
		dbPort = database.getInt("port")
		dbName = database.getString("name")!!

		spawnWorld = VorpalSword.instance.server.getWorld(config.getString("spawnWorld")!!)!!
	}

}
