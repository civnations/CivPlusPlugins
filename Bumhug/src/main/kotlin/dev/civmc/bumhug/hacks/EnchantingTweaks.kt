package dev.civmc.bumhug.hacks

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import dev.civmc.bumhug.Bumhug
import dev.civmc.bumhug.Hack
import org.bukkit.event.Listener

class EnchantingTweaks: Hack(), Listener {
	override val configName = "oldEnchanting"
	override val prettyName = "Old Enchanting"
	
	private val hideEnchantTooltops = config.getBoolean("hideEnchantmentTooltops")
	private val enchantCostMultiplier = config.getDouble("enchantCostMultiplier")
	
	init {
		class EnchantPacketAdapter : PacketAdapter(Bumhug.instance!!, PacketType.Play.Server.WINDOW_DATA) {
			override fun onPacketSending(event: PacketEvent) {
				val property = event.packet.integers.read(1);
				when (property) {
					3, 4, 5, 6 -> event.packet.integers.write(2, -1)
				}
			}
		}
		ProtocolLibrary.getProtocolManager().addPacketListener(EnchantPacketAdapter())
	}
}