package plus.civ.vorpalsword.database

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.block.Container
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import plus.civ.vorpalsword.VorpalSword
import plus.civ.vorpalsword.database.PrisonedPlayer.Companion.imprison
import plus.civ.vorpalsword.executeUpdateAsync
import java.sql.Types
import java.util.*
import kotlin.collections.ArrayList

class PrisonSword private constructor(
		val id: Int,
		location: Location,
		craftedLocation: Location?,
		crafter: OfflinePlayer?,
		craftedDate: Long?,
		howCrafted: String?
) {

	companion object {
		private var lastSerialNumber: Int = 0
		val swords: MutableList<PrisonSword> = mutableListOf()

		/**
		 * Parses the PrisonSword out of an ItemStack.
		 *
		 * @return The sword, or null if the ItemStack was not a valid sword.
		 */
		fun fromItemStack(item: ItemStack): PrisonSword? {
			if (!item.hasItemMeta())
				return null
			if (!item.itemMeta!!.hasLore())
				return null

			return fromItemLore(item.itemMeta!!.lore!!)
		}

		/**
		 * Parses the PrisonSword out of an item's lore.
		 *
		 * This is used inside fromItemStack()
		 *
		 * @return The sword, or null if there was a syntax error.
		 */
		fun fromItemLore(lore: List<String>): PrisonSword? {
			var id: Int? = null

			for (line in lore) {
				// TODO: Allow configuration of serial number format
				if (line.startsWith("Serial Number: ")) {
					id = line.removePrefix("Serial Number: ").toInt()
				}
			}

			if (id == null)
				return null
			else
				return swords[id]
		}

		/**
		 * Returns the serial number of a sword from an ItemStack.
		 *
		 * @return The serial number, or null if it was not a valid sword.
		 */
		fun getSerialNumber(item: ItemStack): Int? {
			if (!item.hasItemMeta())
				return null
			if (!item.itemMeta!!.hasLore())
				return null

			return item.itemMeta!!.lore?.let { getSerialNumber(it) }
		}

		/**
		 * Returns the serial number of a sword from an item's lore.
		 *
		 * Used inside getSerialNumber(ItemStack).
		 *
		 * @return The serial number, or null if there was a syntax error.
		 */
		fun getSerialNumber(lore: List<String>): Int? {
			var id: Int? = null

			for (line in lore) {
				// TODO: Allow configuration of serial number format
				if (line.startsWith("Serial Number: ")) {
					id = line.removePrefix("Serial Number: ").toInt()
				}
			}

			if (id == null)
				return null
			else
				return id
		}

		fun createSword(crafter: OfflinePlayer, craftLocation: Location, howCrafted: String): Pair<PrisonSword, ItemStack> {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
				INSERT INTO swords (world,   x, y, z, crafter_uuid, crafted_date, crafted_world, crafted_x, crafted_y, crafted_z, how_crafted, id)
				VALUES             ("world", 0, 0, 0, ?,            ?,            ?,             ?,         ?,         ?,         ?,           ?)
			""".trimIndent())

			val id = lastSerialNumber

			statement.setString(1, crafter.uniqueId.toString()) // crafter_uuid
			statement.setLong(2, System.currentTimeMillis() / 1000) // crafted_date (unix time)
			statement.setString(3, craftLocation.world!!.name) // crafted_world
			statement.setInt(4, craftLocation.blockX) // crafted_x
			statement.setInt(5, craftLocation.blockY) // crafted_y
			statement.setInt(6, craftLocation.blockZ) // crafted_z
			statement.setString(7, howCrafted) // how_crafted
			statement.setInt(8, lastSerialNumber + 1)

			statement.executeUpdateAsync()

			lastSerialNumber++

			val sword = PrisonSword(
					id,
					Location(VorpalSword.instance.server.getWorld("world"), 0.0, 0.0, 0.0),
					craftLocation,
					crafter,
					System.currentTimeMillis() / 100,
					howCrafted
			)

			swords.add(sword)

			val item = ItemStack(Material.DIAMOND_SWORD)
			val meta = VorpalSword.instance.server.itemFactory.getItemMeta(Material.DIAMOND_SWORD)!!

			meta.lore = sword.generateLore()
			meta.setDisplayName("${ChatColor.RESET}Prison Sword")
			meta.addEnchant(Enchantment.DURABILITY, 10, true)
			meta.isUnbreakable = true
			meta.itemFlags.add(ItemFlag.HIDE_UNBREAKABLE)
			item.itemMeta = meta

			assert(VorpalSword.instance.isSwordItem(item))
			return Pair(sword, item)
		}

		/**
		 * Fills out swords with all prison swords from the database.
		 *
		 * This function includes sync database calls and must be called on the main thread.
		 */
		fun initSwordsList() {
			val swordsStatement = VorpalSword.databaseManager.database.connection.prepareStatement("SELECT * FROM swords")
			val swordResult = swordsStatement.executeQuery() // not async, but this is only ran on plugin startup

			while (swordResult.next()) {
				val id = swordResult.getInt("id")
				if (id > lastSerialNumber)
					lastSerialNumber = id
				val world = swordResult.getString("world")
				val x = swordResult.getInt("x").toDouble()
				val y = swordResult.getInt("y").toDouble()
				val z = swordResult.getInt("z").toDouble()
				val location = Location(VorpalSword.instance.server.getWorld(world), x, y, z)

				val crafterUUID = swordResult.getString("crafter_uuid")
				val crafter = if (crafterUUID != null) VorpalSword.instance.server.getOfflinePlayer(
						UUID.fromString(crafterUUID)) else null
				val craftedDate = swordResult.getLong("crafted_date")
				val craftedWorld = swordResult.getString("crafted_world")
				val craftedX = swordResult.getInt("crafted_x").toDouble()
				val craftedY = swordResult.getInt("crafted_y").toDouble()
				val craftedZ = swordResult.getInt("crafted_z").toDouble()
				val craftedLocation = if (craftedWorld != null)
					Location(VorpalSword.instance.server.getWorld(craftedWorld), craftedX, craftedY, craftedZ) else null
				val howCrafted = swordResult.getString("how_crafted")

				val sword = PrisonSword(id, location, craftedLocation, crafter, craftedDate, howCrafted)

				swords.add(id, sword)
			}
		}
	}

	/**
	 * Send the player to the end with the specified killer.
	 *
	 * @param player The player to send to imprison.
	 *
	 * @param killer The player who killed the other player, or null if there was not one.
	 */
	fun imprisonPlayer(player: OfflinePlayer, killer: OfflinePlayer?) {
		player.imprison(killer, this)
	}

	/**
	 * Generates the lore to be placed onto this sword.
	 *
	 * Called by reevaluateItem().
	 */
	fun generateLore(): List<String> {
		// TODO: Create a better lore format
		val result = ArrayList<String>()

		result.add("Serial Number: $id")
		result.add("Contains the following souls: ")

		val players = StringBuilder()
		for (player in playersInside) {
			players.append(player.player.name)
			players.append(", ")
		}

		result.add(players.toString())

		return result
	}

	/**
	 * Replaces the item in the world with the current version according to the database.
	 *
	 * If the item could not be found, all the players will be freed.
	 *
	 * This function is not called by any function in PrisonSword, and must be called after every change to the sword.
	 *
	 * @return The old item, or null if the item could not be found.
	 */
	fun reevaluateItem(): ItemStack? {
		val lore = generateLore()
		val item = getItemStack() ?: return null

		item.itemMeta!!.lore = lore

		return swapItem(item)
	}

	/**
	 * Frees every player in this sword.
	 */
	fun freeAllPlayers() {
		playersInside.forEach(PrisonedPlayer::freeFromPrison)
	}

	/**
	 * Finds the ItemStack this PrisonSword is in the world.
	 *
	 * If it can not be found, all the players contained within the sword are freed.
	 *
	 * @param freeIfNotFound If all the players should be freed if the item could not be found.
	 *
	 * @return A copy of the item in the world, or null if it was not found.
	 */
	fun getItemStack(freeIfNotFound: Boolean = true): ItemStack? {
		// check last known container
		val location = location

		if (location.block.state !is Container) {
			freeAllPlayers()
			return null
		}

		val container = location.block.state as Container
		for (item in container.inventory.contents) {
			if (item == null)
				continue
			if (VorpalSword.instance.isSwordItem(item) && getSerialNumber(item) == id)
				return item
		}

		// check all online players
		for (player in VorpalSword.instance.server.onlinePlayers) {
			for (item in player.inventory.contents) {
				if (item == null)
					continue
				if (VorpalSword.instance.isSwordItem(item) && getSerialNumber(item) == id)
					return item
			}
		}

		// we've failed, free all the players
		if (freeIfNotFound) {
			freeAllPlayers()
		}
		return null
	}

	/**
	 * Swaps the current prisonsword item with the item passed.
	 *
	 * @return The item that was prevously in the game, or null if the prisonsword could not be found.
	 */
	fun swapItem(item: ItemStack): ItemStack? {
		// check last known container
		val location = location

		if (location.block.state !is Container) {
			freeAllPlayers()
			return null
		}

		val container = location.block.state as Container
		for ((index, containerItem) in container.inventory.contents.withIndex()) {
			if (VorpalSword.instance.isSwordItem(containerItem) && fromItemStack(containerItem)!!.id == id) {
				container.inventory.contents[index] = item
				return containerItem
			}
		}

		// try all online players
		for (player in VorpalSword.instance.server.onlinePlayers) {
			for ((index, containerItem) in player.inventory.contents.withIndex()) {
				if (VorpalSword.instance.isSwordItem(containerItem) && fromItemStack(containerItem)!!.id == id) {
					container.inventory.contents[index] = item
					return containerItem
				}
			}
		}

		// failed to find the item
		freeAllPlayers()
		return null
	}

	/**
	 * The location of the container in the game world that this sword is within.
	 *
	 * Accessing this property makes a database call.
	 *
	 * If the sword is heald by a player, this will be out of date.
	 */
	var location: Location = location
		set(newLocation) {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
                UPDATE swords
                SET x=?, y=?, z=?, world=?
                WHERE id=?
            """.trimIndent())

			statement.setInt(1, newLocation.blockX)
			statement.setInt(2, newLocation.blockY)
			statement.setInt(3, newLocation.blockZ)
			statement.setString(4, newLocation.world!!.name)

			statement.setInt(5, id)

			statement.executeUpdateAsync()

			field = newLocation
		}

	/**
	 * The location that this sword was crafted.
	 */
	var craftedLocation: Location? = craftedLocation
		set(newLocation) {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
                UPDATE swords
                SET crafted_x=?, crafted_y=?, crafted_z=?, world=?
                WHERE id=?
            """.trimIndent())

			if (newLocation == null) {
				statement.setNull(1, Types.INTEGER)
				statement.setNull(2, Types.INTEGER)
				statement.setNull(3, Types.INTEGER)
				statement.setNull(4, Types.VARCHAR)
			} else {
				statement.setInt(1, newLocation.blockX)
				statement.setInt(2, newLocation.blockY)
				statement.setInt(3, newLocation.blockZ)
				statement.setString(4, newLocation.world!!.name)
			}

			statement.setInt(5, id)

			statement.executeUpdateAsync()

			field = newLocation
		}

	/**
	 * The player that crafted this sword.
	 */
	var crafter: OfflinePlayer? = crafter
		set(newCrafter) {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
				UPDATE swords
				SET crafter=?
				WHERE id=?
			""".trimIndent())

			if (newCrafter == null) {
				statement.setNull(1, Types.VARCHAR)
			} else {
				statement.setString(1, newCrafter.uniqueId.toString())
			}

			statement.executeUpdateAsync()

			field = newCrafter
		}

	/**
	 * How this sword was crafted.
	 *
	 * By "factory", "recipe", "meteor", or something else.
	 */
	var howCrafted: String? = howCrafted
		set(newHowCrafted) {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
				UPDATE swords
				SET how_crafted=?
				WHERE id=?
			""".trimIndent())

			if (newHowCrafted == null) {
				statement.setNull(1, Types.VARCHAR)
			} else {
				statement.setString(1, newHowCrafted)
			}

			statement.executeUpdateAsync()

			field = newHowCrafted
		}

	/**
	 * When the sword was crafted, in unix time.
	 */
	var craftedDate: Long? = craftedDate
		set(newDate) {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
				UPDATE swords
				SET crafted_date=?
				WHERE id=?
			""".trimIndent())

			if (newDate == null) {
				statement.setNull(1, Types.BIGINT)
			} else {
				statement.setLong(1, newDate)
			}

			statement.executeUpdateAsync()

			field = newDate
		}

	/**
	 * The players currently imprisoned inside this sword.
	 *
	 * If a player is pearled, this list MUST be manually updated.
	 */
	val playersInside: List<PrisonedPlayer> = mutableListOf()
}
