package plus.civ.vorpalsword

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import plus.civ.vorpalsword.database.*
import plus.civ.vorpalsword.database.PrisonedPlayer.Companion.imprison
import plus.civ.vorpalsword.database.PrisonedPlayer.Companion.isPrisoned
import vg.civcraft.mc.namelayer.NameAPI
import java.lang.NumberFormatException

object VPCommand: CommandExecutor {
	fun stringLocation(location: Location): String = "[${location.world!!.name} ${location.x} ${location.y} ${location.z}]"

	override fun onCommand(sender: CommandSender, command: Command, name: String, args: Array<out String>): Boolean {
		if (args.isEmpty())
			return false

		when (args[0]) {
			"help" -> {
				sender.sendMessage("help, free, locate")
				return true
			}

			"helpadmin" -> {
				sender.sendMessage("helpadmin, locateany, imprison, spwansword")
				return true
			}

			"free" -> {
				if (args.size != 2) {
					sender.sendMessage("${ChatColor.RED}/vp free [player]")
					return true
				}

				val playerName = args[1]
				val uuid = NameAPI.getUUID(playerName)
				if (uuid == null) {
					sender.sendMessage("${ChatColor.RED}Not a valid player.")
					return true
				}
				val player = VorpalSword.instance.server.getOfflinePlayer(uuid)

				if (sender.hasPermission("vorpalsword.free.any")) {
					if (!player.isPrisoned()) {
						sender.sendMessage("${ChatColor.RED}That player is not imprisoned.")
						return true
					}

					val prisonedPlayer = PrisonedPlayer.prisonedPlayers[player]!!

					val sword = prisonedPlayer.sword
					prisonedPlayer.freeFromPrison()
					sword.reevaluateItem()
					return true
				}

				if (sender !is Player) {
					sender.sendMessage("${ChatColor.RED}Must be ran as a player.")
					return true
				}

				if (!VorpalSword.instance.isSwordItem(sender.inventory.itemInMainHand)) {
					sender.sendMessage("${ChatColor.RED}That item isn't a PrisonSword.")
					return true
				}

				val sword = PrisonSword.fromItemStack(sender.inventory.itemInMainHand)!!

				var hasPlayer = false
				sword.playersInside.forEach { if (it.player == player) hasPlayer = true }

				if (!hasPlayer) {
					sender.sendMessage("${ChatColor.RED}That sword does not contain that player.")
					return true
				}

				val prisonedPlayer = PrisonedPlayer.prisonedPlayers[player]!!

				prisonedPlayer.freeFromPrison()
				sword.reevaluateItem()
				sender.sendMessage("${ChatColor.GREEN}Freed ${NameAPI.getCurrentName(player.uniqueId)}.")
				return true
			}

			"locate" -> {
				if (sender !is Player) {
					sender.sendMessage("${ChatColor.RED}Must be ran by a player.")
					return true
				}

				if (!sender.isPrisoned()) {
					sender.sendMessage("${ChatColor.RED}You are not imprisoned.")
					return true
				}

				val prisonedPlayer = PrisonedPlayer.prisonedPlayers[sender]!!

				val player = prisonedPlayer.sword.holdingPlayer
				if (player != null) {
					sender.sendMessage("${ChatColor.GREEN} You are imprisoned in a PrisonSword held by ${ChatColor.AQUA}${player.displayName}${ChatColor.GREEN} at ${ChatColor.AQUA}${stringLocation(player.location)}")
					prisonedPlayer.sword.reevaluateItem()
					return true
				}

				val location = prisonedPlayer.sword.location
				prisonedPlayer.sword.reevaluateItem() // make /vp locate free players if the item can't be found

				sender.sendMessage("${ChatColor.GREEN}You are imprisoned in a PrisonSword at ${ChatColor.AQUA}${stringLocation(location)}")
				return true
			}

			"locateany" -> {
				if (!sender.hasPermission("vorpalsword.locate.any")) {
					return false
				}

				if (args.size != 2) {
					sender.sendMessage("/vp locateany [player]")
					return true
				}


				val playerName = args[1]
				val uuid = NameAPI.getUUID(playerName)
				if (uuid == null) {
					sender.sendMessage("${ChatColor.RED}Not a valid player.")
					return true
				}
				val player = VorpalSword.instance.server.getOfflinePlayer(uuid)

				if (!player.isPrisoned()) {
					sender.sendMessage("${ChatColor.RED}Player is not imprisoned.")
					return true
				}

				val prisonedPlayer = PrisonedPlayer.prisonedPlayers[player]!!

				val sword = prisonedPlayer.sword
				val location = sword.location
				if (sword.reevaluateItem() == null) {
					sender.sendMessage("${ChatColor.GREEN}Could not find sword, freed all players")
				}

				sender.sendMessage("${ChatColor.GREEN}${args[1]} is imprisoned in a PrisonSword at ${ChatColor.AQUA}${stringLocation(location)}")
				return true
			}

			"imprison" -> {
				if (args.size != 3) {
					sender.sendMessage("${ChatColor.RED}/vp imprison [player] [swordid]")
				}

				val playerName = args[1]
				val uuid = NameAPI.getUUID(playerName)
				if (uuid == null) {
					sender.sendMessage("${ChatColor.RED}Not a valid player.")
					return true
				}
				val player = VorpalSword.instance.server.getOfflinePlayer(uuid)

				if (!sender.hasPermission("vorpalsword.forceimprison")) {
					return false
				}

				try {
					player.imprison(null, PrisonSword.swords[args[2].toInt()])
				} catch (e: NumberFormatException) {
					return false
				}

				return true
			}

			"spawnsword" -> {
				if (!sender.hasPermission("vorpalsword.spawnsword")) {
					return false
				}

				if (args.size != 7) {
					sender.sendMessage("${ChatColor.RED}/vp spawnsword [crafter] [crafted_world] [crafted_x] [crafted_y] [crafted_z] [how_crafted]")
					return true
				}

				if (sender !is Player) {
					sender.sendMessage("${ChatColor.RED}Must be ran by a player.")
					return true
				}

				val crafterName = args[1]
				val craftedWorld = args[2]
				val craftedX = args[3]
				val craftedY = args[4]
				val craftedZ = args[5]
				val howCrafted = args[6]

				if (howCrafted.length > 36) {
					sender.sendMessage("${ChatColor.RED}how_crafted must be 36 characters or shorter")
					return true
				}

				val craftedWorldWorld = VorpalSword.instance.server.getWorld(craftedWorld)
				val craftedLocation = Location(craftedWorldWorld, craftedX.toDouble(), craftedY.toDouble(), craftedZ.toDouble())

				val crafterUUID = NameAPI.getUUID(crafterName)
				val crafterPlayer = VorpalSword.instance.server.getOfflinePlayer(crafterUUID)

				val (sword, item) = PrisonSword.createSword(crafterPlayer, craftedLocation, howCrafted)

				sender.sendMessage("${ChatColor.GREEN}Spawned PrisonSword with ID ${sword.id}.")
				sender.inventory.addItem(item)
				return true
			}

			"show" -> {
				if (sender !is Player) {
					return false
				}

				val player = sender as Player
				val item = player.inventory.itemInMainHand
				val sword = PrisonSword.fromItemStack(item)!!
				sender.sendMessage(sword.id.toString())
			}
		}

		return false
	}

}
