package dev.civmc.bumhug.hacks

import dev.civmc.bumhug.Hack
import dev.civmc.bumhug.util.getPlayerByString
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PingCommand: Hack(), CommandExecutor {
    override val configName = "pingCommand"
    override val prettyName = "Ping Command"
    override val commandName = "ping"

    fun pingPlayer(p: Player) {
        p.sendMessage("Pong!")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var player = sender as Player
        if (args.size == 1) {
            if (!sender.hasPermission("bumhug.ping.other")) {
                sender.sendMessage("${ChatColor.RED}You don't have permission to get another player's ping.")
                return true
            }

            val maybePlayer = getPlayerByString(args[0])
            if (maybePlayer == null) {
                sender.sendMessage("${ChatColor.RED}That player was not found")
                return true
            }

            player = maybePlayer
        } else if (args.size > 1)
            return false

        pingPlayer(player)

        return true
    }
}