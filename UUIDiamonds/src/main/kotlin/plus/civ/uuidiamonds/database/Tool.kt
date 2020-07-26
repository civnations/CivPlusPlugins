package plus.civ.uuidiamonds.database

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import plus.civ.uuidiamonds.UUIDiamonds
import java.util.*

class Tool(val id: Int) {
    companion object {
    val TOOL_MATERIALS: Array<Material> = arrayOf(
            Material.DIAMOND_AXE,
            Material.DIAMOND_HOE,
            Material.DIAMOND_PICKAXE,
            Material.DIAMOND_SHOVEL,
            Material.DIAMOND_SWORD,
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS)

        /**
         * @return The item as a Tool object, or null if it was not a tool with a uuid.
         */
        fun fromItemStack(item: ItemStack): Tool? {
            if (item.type !in TOOL_MATERIALS)
                return null
            if (!item.hasItemMeta())
                return null
            if (!item.itemMeta!!.hasLore())
                return null

            for (line in item.itemMeta!!.lore!!) {
                val maybeId = line.toIntOrNull()
                if (maybeId != null) {
                    return Tool(maybeId)
                }
            }

            return null
        }
    }

    /**
     * @return a line of lore that must be on a tool for it to be a UUID tool
     */
    fun generateLoreLine(): String = "${id}"

    val type: Material
        get() {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                SELECT material FROM tools
                WHERE id=?
            """.trimIndent())

            statement.setInt(1, id)

            val result = statement.executeQuery()

            val type = result.getString(1)
            return Material.valueOf(type)
        }

    val crafter: OfflinePlayer
        get() {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                SELECT crafter_uuid FROM tools
                WHERE id=?
            """.trimIndent())

            statement.setInt(1, id)

            val result = statement.executeQuery()

            val uuidStr = result.getString(1)
            val uuid = UUID.fromString(uuidStr)

            return UUIDiamonds.instance.server.getOfflinePlayer(uuid)
        }

    val craftedLocation: Location
        get() {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                SELECT crafted_x, crafted_y, crafted_z, crafted_world FROM tools
                WHERE id=?
            """.trimIndent())

            statement.setInt(1, id)

            val result = statement.executeQuery()

            val x = result.getInt("crafted_x")
            val y = result.getInt("crafted_y")
            val z = result.getInt("crafted_z")
            val worldStr = result.getString("crafted_world")

            val world = UUIDiamonds.instance.server.getWorld(worldStr)

            return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
        }

    val craftedDate: Long
        get() {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                SELECT crafted_date FROM tools
                WHERE id=?
            """.trimIndent())

            statement.setInt(1, id)

            val result = statement.executeQuery()

            return result.getLong(1)
        }

    var isBroken: Boolean
        get() {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                SELECT broken FROM tools
                WHERE id=?
            """.trimIndent())

            statement.setInt(1, id)

            val result = statement.executeQuery()

            return result.getBoolean(1)
        }
        set(value) {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                UPDATE tools
                SET broken=?
                WHERE id=?
            """.trimIndent())

            statement.setBoolean(1, value)
            statement.setInt(2, id)

            statement.executeUpdate()
        }

    val diamonds: Array<Diamond?>
        get() {
            val statement = UUIDiamonds.databaseManager.database.connection.prepareStatement("""
                SELECT id, tool_crafting_slot FROM diamonds 
                WHERE tool_id=?
            """.trimIndent())

            statement.setInt(1, id)

            val result = statement.executeQuery()

            val array: Array<Diamond?> = arrayOfNulls(9)
            while (result.next()) {
                val diamondId = result.getInt("id")
                val slot = result.getInt("tool_crafting_slot")

                array[slot] = Diamond(diamondId)
            }

            return array
        }
}