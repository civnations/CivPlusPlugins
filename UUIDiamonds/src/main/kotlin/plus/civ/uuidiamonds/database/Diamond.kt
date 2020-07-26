package plus.civ.uuidiamonds.database

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import plus.civ.uuidiamonds.UUIDiamonds
import java.sql.Statement
import java.sql.Types
import java.util.*

class Diamond(val id: Int) {
    companion object {
        /**
         * Creates a Diamond object from an ItemStack of a DIAMOND.
         *
         * @return The Diamond object representing the ItemStack, or null if it was not a Diamond with a UUID.
         */
        fun fromItemStack(item: ItemStack): Diamond? {
            if (item.type != Material.DIAMOND) {
                return null
            }

            if (!item.hasItemMeta()) {
                return null
            }

            if (!item.itemMeta!!.hasLore()) {
                return null
            }

            for (line in item.itemMeta!!.lore!!) {
                return Diamond(line.toIntOrNull() ?: return null)
            }

            return null
        }

        fun createDiamond(miner: OfflinePlayer, minedLocation: Location): Diamond {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                INSERT INTO diamonds (miner_uuid, miner_world, mined_x, mined_y, mined_z, mined_date)
                VALUES               (?,          ?,           ?,       ?,       ?,       ?)
            """.trimIndent(), Statement.RETURN_GENERATED_KEYS)

            statement.setString(1, miner.uniqueId.toString()) // miner_uuid

            statement.setString(2, minedLocation.world!!.name) // miner_world
            statement.setInt(3, minedLocation.blockX) // mined_x
            statement.setInt(4, minedLocation.blockY) // mined_y
            statement.setInt(5, minedLocation.blockZ) // mined_z

            statement.setLong(6, System.currentTimeMillis() / 1000) // mined_date (unix time)

            statement.executeUpdate()

            val result = statement.generatedKeys
            result.next()

            val id = result.getInt(1)
            return Diamond(id)
        }
    }

    /**
     * @return a line of lore that must be on a diamond for it to be a UUID diamond
     */
    fun generateLoreLine(): String = "${id}"

    val miner: OfflinePlayer
        get() {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                SELECT miner_uuid FROM diamonds
                WHERE id=?
            """.trimIndent())

            statement.setInt(1, id)

            val result = statement.executeQuery()

            val uuidStr = result.getString(1)
            val uuid = UUID.fromString(uuidStr)

            return UUIDiamonds.instance.server.getOfflinePlayer(uuid)
        }

    val minedLocation: Location
        get() {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                SELECT mined_x, mined_y, mined_z, mined_world FROM diamonds
                WHERE id=?
            """.trimIndent())

            statement.setInt(1, id)

            val result = statement.executeQuery()

            val x = result.getInt("mined_x")
            val y = result.getInt("mined_y")
            val z = result.getInt("mined_z")
            val worldStr = result.getString("mined_world")

            val world = UUIDiamonds.instance.server.getWorld(worldStr)

            return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
        }

    val minedDate: Long
        get() {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                SELECT mined_date FROM diamonds
                WHERE id=?
            """.trimIndent())

            statement.setInt(1, id)

            val result = statement.executeQuery()

            return result.getLong(1)
        }

    var tool: Tool?
        get() {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                SELECT tool_id FROM diamonds
                WHERE id=?
            """.trimIndent())

            statement.setInt(1, id)

            val result = statement.executeQuery()

            val toolId = result.getInt(1)
            if (result.wasNull()) {
                return null
            }

            return Tool(toolId)
        }
        set(value) {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                UPDATE diamonds
                SET tool_id=?
                WHERE id=?
            """.trimIndent())

            if (value == null) {
                statement.setNull(1, Types.INTEGER)
            } else {
                statement.setInt(1, value.id)
            }
            statement.setInt(2, id)

            statement.executeUpdate()
        }

    var craftingSlot: Int?
        get() {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                SELECT tool_crafting_slot FROM diamonds
                WHERE id=?
            """.trimIndent())

            statement.setInt(1, id)

            val result = statement.executeQuery()

            val slot = result.getInt(1)
            if (result.wasNull()) {
                return null
            }

            return slot
        }
        set(value) {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                UPDATE diamonds
                SET tool_crafting_slot=?
                WHERE id=?
            """.trimIndent())

            if (value == null) {
                statement.setNull(1, Types.INTEGER)
            } else {
                statement.setInt(1, value)
            }
            statement.setInt(2, id)

            statement.executeUpdate()
        }
}