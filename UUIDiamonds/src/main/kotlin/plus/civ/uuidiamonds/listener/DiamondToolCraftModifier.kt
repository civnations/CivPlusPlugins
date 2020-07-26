package plus.civ.uuidiamonds.listener

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.meta.ItemMeta
import plus.civ.uuidiamonds.UUIDiamonds
import plus.civ.uuidiamonds.database.Diamond
import plus.civ.uuidiamonds.database.Tool
import java.sql.Statement

class DiamondToolCraftModifier: Listener {
    @EventHandler(ignoreCancelled = true)
    fun onDiamondItemCraft(event: CraftItemEvent) {
        if (event.inventory.result == null)
            return

        if (event.inventory.result!!.type !in Tool.TOOL_MATERIALS)
            return

        val player = event.whoClicked
        val location = event.inventory.location!!

        // identify the uuid diamonds

        val matrix = event.inventory.matrix

        if (matrix[1] != null && matrix[4] != null && matrix[7] != null)
            if (matrix[1].type == Material.DIAMOND && matrix[4].type == Material.DIAMOND && matrix[7].type == Material.STICK)
                return

        val diamondMatrix: Array<Diamond?> = arrayOfNulls(9)
        for ((item, i) in matrix.zip(0..8)) {
            if (item == null)
                continue
            if (item.type != Material.DIAMOND)
                continue

            val diamond = Diamond.fromItemStack(item)
            // ensure only uuid diamonds are used
            if (diamond == null) {
                event.isCancelled = true
                UUIDiamonds.instance.warning("${player.name} tried to craft with non-uuid diamonds: cancling craft")
            }

            diamondMatrix[i] = diamond
        }

        val connection = UUIDiamonds.databaseManager.database.connection
        connection.autoCommit = false

        // create a new tool

        val toolStatement = connection.prepareStatement("""
            INSERT INTO tools (tool_material, crafter_uuid, crafted_world, crafted_x, crafted_y, crafted_z, crafted_date)
            VALUES            (?,             ?,            ?,             ?,         ?,         ?,         ?)
        """.trimIndent(), Statement.RETURN_GENERATED_KEYS)

        toolStatement.setString(1, event.inventory.result!!.type.toString()) // tool_material
        toolStatement.setString(2, event.inventory.viewers[0].uniqueId.toString()) // crafter_uuid

        toolStatement.setString(3, location.world!!.name) // crafted_world
        toolStatement.setInt(4, location.blockX) // crafted_x
        toolStatement.setInt(5, location.blockY) // crafted_y
        toolStatement.setInt(6, location.blockZ) // crafted_z

        toolStatement.setLong(7, System.currentTimeMillis() / 1000) // unix time

        toolStatement.executeUpdate()

        val toolResult = toolStatement.generatedKeys
        toolResult.next()

        val toolId = toolResult.getInt(1)

        for ((diamond, slot) in diamondMatrix.zip(0..8)) {
            if (diamond == null)
                continue

            val diamondStatement = connection.prepareStatement("""
                UPDATE diamonds
                SET tool_id=?, tool_crafting_slot=?
                WHERE id=?
            """.trimIndent())

            diamondStatement.setInt(1, toolId) // tool_id
            diamondStatement.setInt(2, slot) // tool_crafting_slot
            diamondStatement.setInt(3, diamond.id) // id

            diamondStatement.executeUpdate()
        }

        connection.commit()
        connection.autoCommit = true

        val tool = Tool(toolId)

        // set the result to be a uuid tool
        val item = event.inventory.result!!
        val meta: ItemMeta = item.itemMeta ?: UUIDiamonds.instance.server.itemFactory.getItemMeta(item.type)!!

        val lore = meta.lore ?: ArrayList()
        lore.add(tool.generateLoreLine())
        meta.lore = lore
        item.itemMeta = meta

        event.inventory.result = item
    }
}