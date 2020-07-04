package plus.civ.vorpalsword.database

import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import plus.civ.vorpalsword.VorpalSword
import java.sql.Types

/**
 * This function makes a databse call.
 *
 * @return If the player is imprisoned within a PrisonSword.
 */
fun OfflinePlayer.isPrisoned(): Boolean {
	val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
        SELECT COUNT(*) FROM prisoned_players WHERE
          uuid=?
    """.trimIndent())
	statement.setString(1, uniqueId.toString())
	return statement.executeQuery().getInt(1) > 0
}

/**
 * Frees the player from their PrisonSword.
 */
fun OfflinePlayer.freeFromPrison() {
	if (!isPrisoned())
		return

	val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
        DELETE FROM prisoned_players
        WHERE uuid=?
    """.trimIndent())
	statement.setString(1, uniqueId.toString())
	statement.executeUpdate()

	if (this.isOnline) {
		val player = this.player!!
		player.sendMessage("${ChatColor.GREEN}You have been freed.")
	}
}

/**
 * Imprisons the player within a PrisonSword.
 *
 * @param killer The player who killed the player, or null if there is none.
 *
 * @param sword The sword to imprison the player within.
 */
fun OfflinePlayer.imprison(killer: OfflinePlayer?, sword: PrisonSword) {
	if (isPrisoned())
		return

	val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
        INSERT INTO prisoned_players (player_uuid, killer_uuid, sword_id, prisoned_on, last_seen)
        VALUES                       (?          , ?          , ?       , ?          , ?        )
    """.trimIndent())
	statement.setString(1, uniqueId.toString()) // player_uuid
	if (killer != null)
		statement.setString(2, killer.uniqueId.toString()) // killer_uuid
	else
		statement.setNull(2, Types.VARCHAR)
	statement.setInt(3, sword.id) // sword_id
	statement.setLong(4, System.currentTimeMillis() / 1000) // prisoned_on (unix time)
	if (isOnline)
		statement.setLong(5, System.currentTimeMillis() / 1000) // last_seen (unix time)
	else
		statement.setLong(5, 0)
}

/**
 * Returns the sword the player is imprisoned within.
 *
 * @return The PrisonSword, or null if the player is not imprisoned.
 */
fun OfflinePlayer.getPrisonSword(): PrisonSword? {
	if (!isPrisoned())
		return null

	val statement = VorpalSword.databaseManager.database.connection.prepareStatement("""
        SELECT sword_id FROM prisoned_players
        WHERE uuid=?
    """.trimIndent())
	statement.setString(1, uniqueId.toString())
	val result = statement.executeQuery()

	val id = result.getInt("sword_id")
	return PrisonSword(id)
}
