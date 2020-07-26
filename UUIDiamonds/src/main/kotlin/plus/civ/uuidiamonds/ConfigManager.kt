package plus.civ.uuidiamonds

import org.bukkit.configuration.Configuration

class ConfigManager(config: Configuration) {
    val dbUsername: String
    val dbPassword: String
    val dbHost: String
    val dbPort: Int
    val dbName: String

    init {
        val database = config.getConfigurationSection("database")!!
        dbUsername = database.getString("username")!!
        dbPassword = database.getString("password")!!
        dbHost = database.getString("host")!!
        dbPort = database.getInt("port")
        dbName = database.getString("name")!!
    }
}