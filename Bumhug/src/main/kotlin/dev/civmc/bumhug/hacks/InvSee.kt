package dev.civmc.bumhug.hacks

import dev.civmc.bumhug.Bumhug
import dev.civmc.bumhug.Hack
import org.bukkit.command.CommandExecutor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventoryPlayer
import org.bukkit.craftbukkit.v1_12_R1.CraftServer
import net.minecraft.server.v1_12_R1.WorldNBTStorage
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import java.util.UUID
import java.util.logging.Level

class InvSee: Hack(), CommandExecutor {
    override val configName = "invSee"
    override val prettyName = "See other players' Inventories"
    override val commandName = "invsee"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.size != 1) {
            return false
        }

        val playerName = args[0]
        var playerUUID: UUID? = null
        var player: Player? = Bukkit.getPlayer(playerName)

        if (player == null) { // By name failed... is it a UUID?
            try {
                Bumhug.instance.logger.log(Level.INFO, "Using Bukkit by UUID to look up $playerName")
                player = Bukkit.getPlayer(UUID.fromString(playerName))
                playerUUID = UUID.fromString(playerName)
            } catch (iae: IllegalArgumentException) {
                player = null
            }
        }

        if (player == null && playerUUID != null) { // Go deep into NBT.
            val storage = (Bumhug.instance.server as CraftServer).server.worlds[0].dataManager as WorldNBTStorage
            val rawPlayer = storage.getPlayerData(playerUUID.toString())

            if (rawPlayer != null) {
                Bumhug.instance.logger.log(Level.INFO, "Player $playerName found in NBT data, read-only access enabled.")
                sender.sendMessage("Player found via alternate lookup, read-only access enabled.")
            } else {
                sender.sendMessage("Player $playerName does not exist or cannot be opened.")
                return false
            }

            val health = rawPlayer.getFloat("Health")
            val food = rawPlayer.getInt("foodLevel")

            // Fun NMS inventory reconstruction from file data.
            val nmsPlayerInv = net.minecraft.server.v1_12_R1.PlayerInventory(null)
            val playerInvNBT = rawPlayer.getList("Inventory", rawPlayer.typeId.toInt())
            nmsPlayerInv.b(playerInvNBT) // We use this to bypass the Craft code which requires a player object, unlike NMS.
            val playerInv = CraftInventoryPlayer(nmsPlayerInv) as PlayerInventory

            invSee(sender, playerInv, health.toDouble(), food, playerName)
            return true
        }

        if (player == null) {
            sender.sendMessage("Player $playerName does not exist or cannot be opened.")
            return false
        }

        val playerInv = player.inventory
        invSee(sender, playerInv, player.health, player.foodLevel, playerName)

        return true
    }

    fun invSee(sender: CommandSender, playerInv: PlayerInventory, health: Double, food: Int, playerName: String) {
        if (sender !is Player) { // send text only.
            val sb = StringBuffer()
            sb.append(playerName).append("'s\n   Health: ").append(health.toInt() * 2)
            sb.append("\n   Food: ").append(food)
            sb.append("\n   Inventory: ")
            sb.append("\n      Offhand: ").append(playerInv.itemInOffHand)
            sb.append("\n      Helmet: ").append(playerInv.helmet)
            sb.append("\n      Chest: ").append(playerInv.chestplate)
            sb.append("\n      Legs: ").append(playerInv.leggings)
            sb.append("\n      Feet: ").append(playerInv.boots)

            for (slot in 0..35) {
                val item = playerInv.getItem(slot)
                sb.append("\n      ").append(slot).append(":").append(item).append(ChatColor.RESET)
            }

            sender.sendMessage(sb.toString())
        } else {
            val inv = Bukkit.createInventory(sender, 45, playerName + "'s Inventory")

            for (slot in 0..35) {
                val item = playerInv.getItem(slot)
                inv.setItem(slot, item)
            }

            inv.setItem(36, playerInv.getItemInOffHand())
            inv.setItem(38, playerInv.getHelmet())
            inv.setItem(39, playerInv.getChestplate())
            inv.setItem(40, playerInv.getLeggings())
            inv.setItem(41, playerInv.getBoots())

            val healthItem = ItemStack(Material.APPLE, health.toInt() * 2)
            val healthData = healthItem.itemMeta
            healthData.displayName = "Player Health"
            healthItem.itemMeta = healthData
            inv.setItem(43, healthItem)

            val hungerItem = ItemStack(Material.COOKED_BEEF, food)
            val hungerData = hungerItem.itemMeta
            hungerData.displayName = "Player Hunger"
            hungerItem.itemMeta = hungerData
            inv.setItem(44, hungerItem)
            sender.openInventory(inv)
            sender.updateInventory()
        }
    }
}