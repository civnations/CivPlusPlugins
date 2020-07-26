package plus.civ.uuidiamonds.listener

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent

class DisableCraftDiamond: Listener {
    @EventHandler
    fun onDiamondCraftPrepare(event: PrepareItemCraftEvent) {
        if (event.recipe == null)
            return

        // disable crafting diamond blocks
        if (event.recipe!!.result.type == Material.DIAMOND_BLOCK) {
            event.inventory.result = null
        }

        // disable uncrafting diamond blocks
        if (event.recipe!!.result.type == Material.DIAMOND) {
            event.inventory.result = null
        }
    }

    @EventHandler
    fun onDiamondCraft(event: CraftItemEvent) {
        // disable crafting diamond blocks
        if (event.recipe.result.type == Material.DIAMOND_BLOCK) {
            event.isCancelled = true
        }

        // disable uncrafting diamond blocks
        if (event.recipe.result.type == Material.DIAMOND) {
            event.isCancelled = true
        }
    }
}