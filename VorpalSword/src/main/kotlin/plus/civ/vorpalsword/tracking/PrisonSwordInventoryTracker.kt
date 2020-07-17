package plus.civ.vorpalsword.tracking

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.PlayerInventory
import plus.civ.vorpalsword.VorpalSword
import plus.civ.vorpalsword.database.PrisonSword

/**
 * Tracks inventory movements for PrisonSwords and updates the database if a PrisonSword has moved.
 */
object PrisonSwordInventoryTracker: Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun inventoryClick(event: InventoryClickEvent) {
        // we might have to run these a tick later, not sure
        evalInventory(event.view.bottomInventory)
        evalInventory(event.view.topInventory)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun inventoryDrag(event: InventoryDragEvent) {
        evalInventory(event.view.bottomInventory)
        evalInventory(event.view.topInventory)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun inventoryMoveItem(event: InventoryMoveItemEvent) {
        if (!VorpalSword.instance.isSwordItem(event.item)) {
            return
        }

        val newLocation = event.destination.location ?: return
        val sword = PrisonSword.fromItemStack(event.item) ?: return

        sword.location = newLocation
    }

    fun evalInventory(inventory: Inventory) {
        if (inventory is PlayerInventory) {
            return
        }

        for (item in inventory.contents) {
            if (!VorpalSword.instance.isSwordItem(item)) {
                continue
            }

            if (inventory.location == null) {
                continue
            }

            val sword = PrisonSword.fromItemStack(item) ?: continue

            sword.location = inventory.location!!
        }
    }
}
