package plus.civ.uuidiamonds.database

import plus.civ.uuidiamonds.UUIDiamonds
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource

class DatabaseManager {
    val database = ManagedDatasource(UUIDiamonds.instance,
            UUIDiamonds.configManager.dbUsername,
            UUIDiamonds.configManager.dbPassword,
            UUIDiamonds.configManager.dbHost,
            UUIDiamonds.configManager.dbPort,
            UUIDiamonds.configManager.dbName,
            5,
            6000,
            600000,
            7200000)

    init {
        val diamondsTable = """
            CREATE TABLE diamonds (
                id int NOT NULL AUTO_INCREMENT,
                
                -- data logging 
                miner_uuid varchar(36),
                mined_world varchar(36),
                mined_x int,
                mined_y int,
                mined_z int,
                mined_date long, -- unix time
                
                tool_id int, -- The tool the diamond has been crafted into (null if not yet crafted)
                tool_crafting_slot int, -- The slot in the crafting table the diamond took up (nullable)
                
                PRIMARY KEY (id),
                FOREIGN KEY (tool_id) REFERENCES tools(id),
            )
        """.trimIndent()

        val toolsTable = """
            CREATE TABLE tools (
                id int NOT NULL AUTO_INCREMENT,
                
                tool_material varchar(36) NOT NULL,
                
                -- data logging
                crafter_uuid varchar(36),
                crafted_world varchar(36),
                crafted_x int,
                crafted_y int,
                crafted_z int,
                crafted_date long, -- unix time
                
                broken bool DEFAULT false NOT NULL, -- if the tool has been broken
                
                PRIMARY KEY (id)
            )
        """.trimIndent()

        database.registerMigration(1, false, diamondsTable, toolsTable)

        database.updateDatabase()
    }
}