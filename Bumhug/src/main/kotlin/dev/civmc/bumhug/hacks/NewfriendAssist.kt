package dev.civmc.bumhug.hacks

import dev.civmc.bumhug.Bumhug
import dev.civmc.bumhug.Hack
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

public class NewfriendAssist: Hack(), Listener {
	override val configName = "newfriendAssist"
	override val prettyName = "Newfriend Assist"
	
	private val message = config.getString("message")
	private val newfriendKit = ArrayList<ItemStack>()
	init {
		val kitSection = config.getConfigurationSection("newfriendKit")
		if (kitSection != null) {
			for (key in kitSection.getKeys(false)) {
				val matName = kitSection.getString(key + ".material")!!
				val mat = Material.getMaterial(matName)!!
				val amount = kitSection.getInt(key + ".amount", 1)
				val lore = kitSection.getList(key + ".lore", ArrayList<String>()) as ArrayList<String>
				val name = kitSection.getString(key + ".name")
				
				val stack = ItemStack(mat, amount)
				stack.amount = amount
				val meta = stack.itemMeta!!
				if (lore.size != 0) {
					meta.lore = lore
					if (name != null) {
						meta.setDisplayName(name)
					}
				}
				stack.itemMeta = meta
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onNewfriendJoin(event: PlayerJoinEvent) {
		if (event.player.hasPlayedBefore()) {
			return
		}
		if (message != null) {
			val cleanMessage = ChatColor.translateAlternateColorCodes('&',
				message.replace("%Name%", event.player.displayName)
			)
			for (player in Bumhug.instance.server.onlinePlayers) {
				player.sendMessage(cleanMessage)
			}
		}
		if (newfriendKit.size > 0) {
			for (stack in newfriendKit) {
				event.player.inventory.addItem(ItemStack(stack))
			}
		}
	}
}