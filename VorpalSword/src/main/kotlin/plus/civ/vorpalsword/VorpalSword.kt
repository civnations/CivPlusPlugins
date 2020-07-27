package plus.civ.vorpalsword

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import plus.civ.vorpalsword.database.DatabaseManager
import plus.civ.vorpalsword.database.PrisonSword
import plus.civ.vorpalsword.database.PrisonedPlayer
import plus.civ.vorpalsword.restriction.PlayerCantLeaveEndRestriction
import plus.civ.vorpalsword.restriction.PlayerEndRestriction
import plus.civ.vorpalsword.tracking.FreeManInEndMessage
import plus.civ.vorpalsword.tracking.PrisonOnKill
import plus.civ.vorpalsword.tracking.PrisonSwordInventoryTracker
import vg.civcraft.mc.civmodcore.ACivMod
import java.sql.PreparedStatement
import java.util.function.Predicate

class VorpalSword: ACivMod() {
	companion object {
		val instance: VorpalSword
			get() = instanceStorage!!
		private var instanceStorage: VorpalSword? = null

		val configManager: ConfigManager
			get() = configManagerStorage!!
		private var configManagerStorage: ConfigManager? = null

		val databaseManager: DatabaseManager
			get() = databaseManagerStorage!!
		private var databaseManagerStorage: DatabaseManager? = null
	}

	override fun onEnable() {
		super.onEnable()
		instanceStorage = this
		configManagerStorage = ConfigManager(config)
		databaseManagerStorage = DatabaseManager()

		PrisonSword.initSwordsList()
		PrisonedPlayer.initPrisonedPlayers()

		registerListener(PlayerEndRestriction)
		registerListener(PrisonSwordInventoryTracker)
		registerListener(PlayerCantLeaveEndRestriction)
		registerListener(PrisonOnKill)
		registerListener(FreeManInEndMessage)

		getCommand("vp")!!.setExecutor(VPCommand)
	}

	//override fun getPluginName(): String {
	//    return "VorpalSword"
	//}

	/**
	 * Regenerates all the PrisonSword ItemStacks in the world.
	 */
	fun reevaluateSwords() {
		PrisonSword.swords.forEach { it.reevaluateItem() }
	}

	/**
	 * @return If the item is a PrisonSword.
	 */
	fun isSwordItem(item: ItemStack): Boolean {
		if (!item.hasItemMeta())
			return false
		if (!item.itemMeta!!.hasLore())
			return false

		return item.type == Material.DIAMOND_SWORD &&
				item.itemMeta!!.persistentDataContainer.has(PrisonSword.swordIdKey, PersistentDataType.INTEGER)
	}
}

internal fun PreparedStatement.executeUpdateAsync() {
	VorpalSword.instance.server.scheduler.runTaskAsynchronously(VorpalSword.instance, Runnable {
		executeUpdate()
	})
}

fun romanEncode(number: Int): String {
	val romanNumerals = mapOf(
			1000 to "M",
			900 to "CM",
			500 to "D",
			400 to "CD",
			100 to "C",
			90 to "XC",
			50 to "L",
			40 to "XL",
			10 to "X",
			9 to "IX",
			5 to "V",
			4 to "IV",
			1 to "I"
	)

	if (number == 0)
		return ""

	var num = number
	val result = StringBuffer()
	for ((multiple, numeral) in romanNumerals.entries) {
		while (num >= multiple) {
			num -= multiple
			result.append(numeral)
		}
	}
	return result.toString()
}

