package plus.civ.uuidiamonds.listener

import com.github.devotedmc.hiddenore.events.HiddenOreEvent
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import plus.civ.uuidiamonds.UUIDiamonds
import plus.civ.uuidiamonds.database.Diamond
import java.util.*

class HiddenOreDiamondModifier: Listener {
    @EventHandler(ignoreCancelled = true)
    fun hiddenOreDiamond(event: HiddenOreEvent) {
        if (event.drops.stream().noneMatch { it.type == Material.DIAMOND })
            return

        val drops = event.drops

        event.drops = drops.map(fun(item: ItemStack): ItemStack {
            if (item.type != Material.DIAMOND)
                return item

            val diamond = Diamond.createDiamond(event.player, event.dropLocation)
            val meta = item.itemMeta ?: UUIDiamonds.instance.server.itemFactory.getItemMeta(Material.DIAMOND)!!
            val lore = meta.lore ?: ArrayList<String>()
            lore.add(diamond.generateLoreLine())
            meta.lore = lore
            item.itemMeta = meta

            return item
        })
    }
}