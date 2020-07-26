package plus.civ.uuidiamonds.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemBreakEvent
import plus.civ.uuidiamonds.database.Tool

class DiamondToolBrokenListener: Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun diamondToolBroken(event: PlayerItemBreakEvent) {
        val tool = Tool.fromItemStack(event.brokenItem) ?: return

        tool.isBroken = true
    }
}