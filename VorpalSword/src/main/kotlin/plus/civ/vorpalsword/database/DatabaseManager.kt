package plus.civ.vorpalsword.database

import plus.civ.vorpalsword.VorpalSword
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource

class DatabaseManager {
	val database = ManagedDatasource(VorpalSword.instance,
			VorpalSword.configManager.dbUsername,
			VorpalSword.configManager.dbPassword,
			VorpalSword.configManager.dbHost,
			VorpalSword.configManager.dbPort,
			VorpalSword.configManager.dbName,
			5,
			6000,
			600000,
			7200000)

	init {
		val swordsTable = """
            CREATE TABLE swords (
              id INT NOT NULL AUTO_INCREMENT,
              world varchar(36) NOT NULL,
              x int NOT NULL,
              y int NOT NULL,
              z int NOT NULL,
              
              -- semi-useless data collection ahead
              crafter_uuid varchar(36),
              crafted_date long,
              crafted_x int,
              crafted_y int,
              crafted_z int,
              crafted_world varchar(36),
              how_crafted varchar(36), -- how the sword was crafted: factory, recipe, meteor, etc
            
              PRIMARY KEY (id)
            )
            
        """.trimIndent()

		val prisonedPlayersTable = """
            CREATE TABLE prisoned_players (
              id INT NOT NULL AUTO_INCREMENT,
              player_uuid varchar(36) NOT NULL,
              killer_uuid varchar(36), 
              sword_id INT NOT NULL,
              prisoned_on long NOT NULL, -- unix time 
              last_seen long NOT NULL, -- unix time
              freed_offline bool,
              
              PRIMARY KEY (id),
              FOREIGN KEY (sword_id) REFRENCES swords(id)
            )
        """.trimIndent()

		database.registerMigration(0, false, swordsTable, prisonedPlayersTable)
		database.updateDatabase()
	}
}