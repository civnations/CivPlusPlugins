package dev.civmc.bumhug.hacks

import dev.civmc.bumhug.Bumhug
import dev.civmc.bumhug.Hack
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import vg.civcraft.mc.citadel.Citadel
import dev.civmc.bumhug.Depend

@Depend("Citadel")
public class RaiderAnnounce: Hack(), Listener {
	override val configName = "raiderAnnounce"
	override val prettyName = "Raider Announce"

	private val message = config.getString("message") ?: "&4%Name% is raiding a chest at %X%, %Y%, %Z%."
	// seconds
	private val messageDelay = config.getInt("delay")

	private val lastAlertSent = HashMap<Player, Long>()

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	fun onReinforcementBreak(event: BlockBreakEvent) {
		val man = Citadel.getInstance().getReinforcementManager()
		if (man == null) {
			return
		}
		val isReinforced = man.getReinforcement(event.block) != null
		if (isReinforced && (event.block.type == Material.CHEST || event.block.type == Material.TRAPPED_CHEST)) {
			val last: Long? = lastAlertSent.get(event.player)
			val now = System.currentTimeMillis()
			if (last == null) {
				lastAlertSent.put(event.player, now)
			} else if (now - last < messageDelay) {
				return
			} else {
				lastAlertSent.put(event.player, now)
			}

			val cleanMessage = ChatColor.translateAlternateColorCodes('&',
				message
				.replace("%Name%", event.player.displayName)
				.replace("%X%", event.block.location.x.toString())
				.replace("%Y%", event.block.location.y.toString())
				.replace("%Z%", event.block.location.z.toString())
			)
			Bumhug.instance.broadcastToPerm("broadcastRaiding", cleanMessage)
		}
	}
}
