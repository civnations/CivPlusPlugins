package dev.civmc.bumhug.hacks

import dev.civmc.bumhug.Bumhug
import dev.civmc.bumhug.Depend
import dev.civmc.bumhug.Hack
import net.minelink.ctplus.event.PlayerCombatTagEvent
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.UUID
import java.util.logging.Level

@Depend("CombatTagPlus")
class CTAnnounce: Hack(), Listener {
	override val configName = "ctAnnounce"
	override val prettyName = "CTAnnounce"
	
	private val delay = config.getLong("delay", 10000)
	private val message = config.getString("message", "&4%Victim% was combat tagged by %Attacker%")!!
	
	private val lastCTAnnounce = HashMap<UUID, Long>()
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	fun onCombatTag(event: PlayerCombatTagEvent) {
		if (event.victim == null || event.attacker == null) return
		
		// Throttle broadcast frequency
		val lastTag = lastCTAnnounce.get(event.getVictim().getUniqueId())
		val now = System.currentTimeMillis()
		if (lastTag != null && now - lastTag < delay) return
		lastCTAnnounce.put(event.getVictim().getUniqueId(), now)

		val cleanMessage = ChatColor.translateAlternateColorCodes('&',
			message
			.replace("%Victim%", event.victim.displayName)
			.replace("%Attacker%", event.attacker.displayName)
		)
		Bumhug.instance.logger.log(Level.INFO, cleanMessage)
		Bumhug.instance.broadcastToPerm("broadcastCombat", cleanMessage)
	}
}