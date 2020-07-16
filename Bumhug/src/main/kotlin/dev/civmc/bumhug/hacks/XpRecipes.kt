package dev.civmc.bumhug.hacks

import dev.civmc.bumhug.Bumhug
import dev.civmc.bumhug.Hack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe

// Bottles to emeralds and back
class XpRecipes: Hack() {
    override val configName = "xpRecipes"
    override val prettyName = "XP Recipes"

    override fun onEnable() {
        // Emerald to Bottles recipe
        val nineBottles = ItemStack(Material.EXPERIENCE_BOTTLE, 9)
        val emeraldToBottlesKey = NamespacedKey(Bumhug.instance, "emerald_to_bottles")
        val emeraldToBottles = ShapelessRecipe(emeraldToBottlesKey, nineBottles)
        emeraldToBottles.addIngredient(1, Material.EMERALD)

        // Bottles to Emerald recipe
        val emerald = ItemStack(Material.EMERALD, 1)
        val bottlesToEmeraldKey = NamespacedKey(Bumhug.instance, "bottles_to_emerald")
        val bottlesToEmerald = ShapelessRecipe(bottlesToEmeraldKey, emerald)
        bottlesToEmerald.addIngredient(9, Material.EXPERIENCE_BOTTLE)

        // Add both recipes
        Bukkit.addRecipe(emeraldToBottles)
        Bukkit.addRecipe(bottlesToEmerald)
    }
}