package plus.civ.vorpalsword.tracking

import net.minelink.ctplus.CombatTagPlus
import net.minelink.ctplus.event.PlayerCombatTagEvent
import org.bukkit.OfflinePlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import plus.civ.vorpalsword.VorpalSword
import plus.civ.vorpalsword.database.PrisonSword
import plus.civ.vorpalsword.database.imprison
import plus.civ.vorpalsword.database.isPrisoned

/**
 * Imprisones players if they are killed with a PrisonSword.
 */
class PrisonOnKill: Listener {
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val victim = event.entity
        if (victim.isPrisoned()) {
            return // player was killed in the end
        }

        val combatTag = VorpalSword.instance.server.pluginManager.getPlugin("CombatTagPlus") as CombatTagPlus

        if (!combatTag.tagManager.isTagged(victim.uniqueId)) {
            return
        }

		val tag = combatTag.tagManager.getTag(victim.uniqueId)

        val attacker = VorpalSword.instance.server.getPlayer(tag.attackerId) ?: return

        val attackerInventory = attacker.player?.inventory!!.contents

        for ((hotbarSlot, item) in (0..8).zip(attackerInventory)) {
            if (!VorpalSword.instance.isSwordItem(item)) {
                continue
            }

            val sword = PrisonSword.fromItemStack(item) ?: continue
            victim.imprison(attacker, sword)
            sword.reevaluateItem()
        }
    }
}
