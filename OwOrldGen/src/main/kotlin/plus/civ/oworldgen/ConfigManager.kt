package plus.civ.oworldgen

import org.bukkit.block.Biome
import org.bukkit.Material
import java.util.logging.Level
import org.bukkit.configuration.ConfigurationSection
import java.awt.Color

class ConfigManager(conf: ConfigurationSection) {
	private class BiomeInfo(val topblock: Material, val fineCoeff: Double, val hillCoeff: Double, val broadCoeff: Double) {}
	private val biomeInfos = HashMap<Biome, BiomeInfo>()
    init {
        val biomesSection = conf.getConfigurationSection("biomes")!!
        for (biomeName in biomesSection.getKeys(false)) {
            val biomeSection = biomesSection.getConfigurationSection(biomeName)!!
			val bukkitBiome: Biome = Biome.valueOf(biomeName)
			try {
				val topblock: Material = Material.valueOf(biomeSection.getString("topblock") ?: "GRASS_BLOCK")
			} catch (e: IllegalArgumentException) {
				OwOrldGen.instance.getLogger().log(Level.SEVERE, "The topblock in the config for the biome " + bukkitBiome.toString() + " is not a material.")
			}
			val fineCoeff = biomeSection.getString("fine")
			val hillCoeff = biomeSection.getString("hill")
			val broadCoeff = biomeSection.getString("broad")
			biomeInfos.insert(bukkitBiome, BiomeInfo(topblock, fineCoeff, hillCoeff, broadCoeff))
        }
    }
	
	public val seaLevel = conf.getInt("sea_level")
	public val groundPadding = conf.getInt("ground_padding")
	
	public fun getTopblock(biome: Biome): Material {
		return biomeInfos.get(biome).topblock
	}
	
	public fun get
}
