package plus.civ.uuidiamonds.command

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plus.civ.uuidiamonds.UUIDiamonds
import plus.civ.uuidiamonds.database.Diamond

class SpawnDiamondCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, name: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}Must be ran by a player.")
            return true
        }

        val diamond = Diamond.createDiamond(sender, sender.location)
        val diamondItem = ItemStack(Material.DIAMOND)
        val meta = UUIDiamonds.instance.server.itemFactory.getItemMeta(Material.DIAMOND)!!
        val lore = ArrayList<String>()
        lore.add(diamond.generateLoreLine())
        meta.lore = lore
        diamondItem.itemMeta = meta

        sender.inventory.addItem(diamondItem)
        sender.sendMessage("${ChatColor.GREEN}Gave diamond with id ${ChatColor.AQUA}[${diamond.id}]")

        return true
    }
}