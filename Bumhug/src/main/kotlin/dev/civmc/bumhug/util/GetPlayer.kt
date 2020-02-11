package dev.civmc.bumhug.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * Searches for an online player by a string, usually the argument to a command.
 *
 * @param str The player's name/uuid/whatever may identify the player in the future
 * @return The player, or null if the player wasn't found
 */
fun getPlayerByString(str: String): Player? {
    try {
        return Bukkit.getPlayer(UUID.fromString(str))
    } catch (e: IllegalArgumentException) {
        // TODO: Use NameAPI to get the player by nickname instead of bukkit name.
        return Bukkit.getPlayer(str)
    }
}