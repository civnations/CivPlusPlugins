package plus.civ.vorpalsword

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import plus.civ.vorpalsword.database.DatabaseManager
import plus.civ.vorpalsword.database.PrisonSword
import plus.civ.vorpalsword.restriction.PlayerCantLeaveEndRestriction
import plus.civ.vorpalsword.restriction.PlayerEndRestriction
import plus.civ.vorpalsword.tracking.PrisonOnKill
import plus.civ.vorpalsword.tracking.PrisonSwordInventoryTracker
import vg.civcraft.mc.civmodcore.ACivMod
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

		registerListener(PlayerEndRestriction())
		registerListener(PrisonSwordInventoryTracker())
		registerListener(PlayerCantLeaveEndRestriction())
		registerListener(PrisonOnKill())

		getCommand("vp")!!.setExecutor(VPCommand())
		
		this.saveDefaultConfig()
	}

	//override fun getPluginName(): String {
	//    return "VorpalSword"
	//}

	/**
	 * Regenerates all the PrisonSword ItemStacks in the world.
	 */
	fun reevaluateSwords() {
		val statement = databaseManager.database.connection.prepareStatement("""
			SELECT id FROM swords
		""".trimIndent())
		val result = statement.executeQuery()

		while (result.next()) {
			val id = result.getInt("id")
			val sword = PrisonSword(id)

			sword.reevaluateItem()
		}
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
				item.itemMeta!!.lore!!.stream().anyMatch { line -> line.startsWith("Serial Number: ")}
	}
}
