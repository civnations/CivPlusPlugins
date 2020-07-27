package plus.civ.vorpalsword.database

import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import plus.civ.vorpalsword.VorpalSword
import plus.civ.vorpalsword.executeUpdateAsync
import java.sql.Statement
import java.sql.Types
import java.util.*

/**
 * Represents a player that is in a prison pearl.
 */
class PrisonedPlayer private constructor(
		val player: OfflinePlayer,
		val sword: PrisonSword,
		val killer: OfflinePlayer?,
		val prisonedOn: Long,
		lastSeen: Long,
		val id: Int
) {
	companion object {
		val prisonedPlayers: MutableMap<OfflinePlayer, PrisonedPlayer> = Collections.synchronizedMap(mutableMapOf())
		private var lastId: Int = 0

		fun initPrisonedPlayers() {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
				SELECT * FROM prisoned_players
			""".trimIndent())

			val result = statement.executeQuery()

			while (result.next()) {
				val id = result.getInt("id")
				if (id > lastId)
					lastId = id

				val player = VorpalSword.instance.server.getOfflinePlayer(
						UUID.fromString(result.getString("player_uuid")))
				val killerUUID = result.getString("killer_uuid") // not inlined for null check
				val killer = if (killerUUID != null) VorpalSword.instance.server.getOfflinePlayer(
						UUID.fromString(killerUUID)) else null
				val sword = PrisonSword.swords[result.getInt("sword_id")]
				val prisonedOn = result.getLong("prisoned_on")
				val lastSeen = result.getLong("last_seen")

				val prisonedPlayer = PrisonedPlayer(player, sword, killer, prisonedOn, lastSeen, id)

				prisonedPlayers[player] = prisonedPlayer
			}
		}

		/**
		 * Imprisons the player within a PrisonSword.
		 *
		 * @param killer The player who killed the player, or null if there is none.
		 *
		 * @param sword The sword to imprison the player within.
		 */
		fun OfflinePlayer.imprison(killer: OfflinePlayer?, sword: PrisonSword): PrisonedPlayer {
			if (isPrisoned())
				return prisonedPlayers[this]!!

			val now = System.currentTimeMillis() / 1000 // unix time
			val id = lastId
			lastId++

			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
        		INSERT INTO prisoned_players (player_uuid, killer_uuid, sword_id, prisoned_on, last_seen, id)
        		VALUES                       (?          , ?          , ?       , ?          , ?        , ?)
    		""".trimIndent(), Statement.RETURN_GENERATED_KEYS)
			statement.setString(1, uniqueId.toString()) // player_uuid
			if (killer != null)
				statement.setString(2, killer.uniqueId.toString()) // killer_uuid
			else
				statement.setNull(2, Types.VARCHAR)
			statement.setInt(3, sword.id) // sword_id
			statement.setLong(4, now) // prisoned_on (unix time)
			if (isOnline)
				statement.setLong(5, now) // last_seen (unix time)
			else
				statement.setLong(5, 0)
			statement.setInt(6, id)

			statement.executeUpdateAsync()

			val prisonedPlayer = PrisonedPlayer(this, sword, killer, now, now, id)
			prisonedPlayers[this] = prisonedPlayer

			return prisonedPlayer
		}

		fun OfflinePlayer.isPrisoned(): Boolean {
			return prisonedPlayers.contains(this)
		}
	}

	/**
	 * Frees the player from their PrisonSword.
	 */
	fun freeFromPrison() {
		if (!player.isPrisoned())
			return

		prisonedPlayers.remove(player)

		val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
        	DELETE FROM prisoned_players
        	WHERE uuid=?
    	""".trimIndent())
		statement.setString(1, player.uniqueId.toString())

		statement.executeUpdateAsync()

		if (player.isOnline) {
			val player: Player = player.player!!
			player.sendMessage("${ChatColor.GREEN}You have been freed.")
		}
	}

	var lastSeen: Long = lastSeen
		set(newLastSeen) {
			val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
				UPDATE prisoned_players
				SET last_seen=?
				WHERE id=?
			""".trimIndent())

			statement.setLong(1, newLastSeen)
			statement.setInt(2, id)

			statement.executeUpdateAsync()

			field = newLastSeen
		}
}
