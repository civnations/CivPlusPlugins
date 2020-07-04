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
import java.sql.Statement
import java.util.*
import kotlin.collections.ArrayList

class PrisonSword(val id: Int) {
	companion object {
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
				return PrisonSword(id)
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
				INSERT INTO swords (world,   x, y, z, crafter_uuid, crafted_date, crafted_world, crafted_x, crafted_y, crafted_z, how_crafted)
				VALUES             ("world", 0, 0, 0, ?,            ?,            ?,             ?,         ?,         ?,         ?)
			""".trimIndent(), Statement.RETURN_GENERATED_KEYS)

			statement.setString(1, crafter.uniqueId.toString()) // crafter_uuid
			statement.setLong(2, System.currentTimeMillis() / 1000) // crafted_date (unix time)
			statement.setString(3, craftLocation.world!!.name) // crafted_world
			statement.setInt(4, craftLocation.blockX) // crafted_x
			statement.setInt(5, craftLocation.blockY) // crafted_y
			statement.setInt(6, craftLocation.blockZ) // crafted_z
			statement.setString(7, howCrafted) // how_crafted

			statement.executeUpdate()

			val result = statement.generatedKeys
			result.next()
			val id = result.getInt(1)

			val sword = PrisonSword(id)
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
	 * Free a specific player from this sword.
	 */
	fun freePlayer(player: OfflinePlayer) {
		player.freeFromPrison()
	}

	/**
	 * Generates the lore to be placed onto this sword.
	 *
	 * Called by reevaluateItem().
	 */
	fun generateLore(): List<String> {
		// TODO: Create a better lore format
		val result = ArrayList<String>()

		result.add("Serial Number: ${id}")
		result.add("Contains the following souls: ")

		val players = StringBuilder()
		for (player in playersInside) {
			players.append(player.name)
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
		playersInside.forEach(OfflinePlayer::freeFromPrison)
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

		if (!(location.block.state is Container)) {
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

		if (!(location.block.state is Container)) {
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
	var location: Location
		get() {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
                SELECT x, y, z, world FROM swords
                WHERE id=?
            """.trimIndent())
			statement.setInt(1, id)
			val result = statement.executeQuery()
			val x = result.getInt("x")
			val y = result.getInt("y")
			val z = result.getInt("z")
			val world = result.getString("world")
			return Location(VorpalSword.instance.server.getWorld(world), x.toDouble(), y.toDouble(), z.toDouble())
		}
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

			statement.executeUpdate()
		}

	/**
	 * The location that this sword was crafted.
	 */
	val craftedLocation: Location?
		get() {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
                SELECT crafted_x, crafted_y, crafted_z, crafted_world FROM swords
                WHERE id=?
            """.trimIndent())
			statement.setInt(1, id)
			val result = statement.executeQuery()
			val x = result.getInt("crafted_x")
			val y = result.getInt("crafted_y")
			val z = result.getInt("crafted_z")
			val world = result.getString("crafted_world")
			return Location(VorpalSword.instance.server.getWorld(world), x.toDouble(), y.toDouble(), z.toDouble())
		}

	/**
	 * The player that crafted this sword.
	 */
	val crafter: OfflinePlayer?
		get() {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
                SELECT crafter_uuid FROM swords
                WHERE id=?
            """.trimIndent())
			statement.setInt(1, id)
			val result = statement.executeQuery()
			val uuidStr = result.getString(1) ?: return null
			return VorpalSword.instance.server.getOfflinePlayer(UUID.fromString(uuidStr))
		}

	/**
	 * How this sword was crafted.
	 *
	 * By "factory", "recipe", "meteor", or something else.
	 */
	val howCrafted: String?
		get() {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
                SELECT how_crafted FROM swords
                WHERE id=?
            """.trimIndent())
			statement.setInt(1, id)
			val result = statement.executeQuery()
			return result.getString(1)
		}

	/**
	 * The players currently imprisoned inside this sword.
	 */
	val playersInside: List<OfflinePlayer>
		get() {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
            SELECT uuid FROM prisoned_players
            INNER JOIN swords ON prisoned_players.sword_id=swords.id
            WHERE swords.id=? 
        """.trimIndent())
			statement.setInt(1, id)
			val result = statement.executeQuery()

			val prisonedPlayers = ArrayList<OfflinePlayer>()

			while (result.next()) {
				val uuid: String = result.getString("uuid")
				val offlinePlayer = VorpalSword.instance.server.getOfflinePlayer(UUID.fromString(uuid))
				prisonedPlayers.add(offlinePlayer)
			}

			return prisonedPlayers
		}
}
