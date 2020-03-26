package plus.civ.oworldgen

import org.bukkit.block.Biome
import org.bukkit.Material
import java.util.logging.Level
import org.bukkit.configuration.ConfigurationSection
import java.awt.Color

class ConfigManager(conf: ConfigurationSection) {
    private val biomeColors = HashMap<Color, Biome>()
	private val biomeTopblocks = HashMap<Biome, Material>()
    init {
        val biomesSection = conf.getConfigurationSection("biomes")!!
        for (biomeName in biomesSection.getKeys(false)) {
            val biomeSection = biomesSection.getConfigurationSection(biomeName)!!
            val colorHexString = biomeSection.getString("color")
			val color: Color = Color.decode(colorHexString)
			val bukkitBiome: Biome = Biome.valueOf(biomeName)
			biomeColors.put(color, bukkitBiome)
			
			try {
				val topblock: Material = Material.valueOf(biomeSection.getString("topblock") ?: "")
				biomeTopblocks.put(bukkitBiome, topblock)
			} catch (e: IllegalArgumentException) {
				OwOrldGen.instance.getLogger().log(Level.SEVERE, "The topblock in the config for the biome " + bukkitBiome.toString() + " is not a material.")
			}
        }
    }
	
	public val seaLevel = conf.getInt("sea_level")
	public val groundPadding = conf.getInt("ground_padding")
	
    private val biomeMapImagePath: String = conf.getString("biome_image")!!
	private val heightMapImagePath: String = conf.getString("height_image")!!
	
    public val mapdata: Map = Map(biomeMapImagePath, heightMapImagePath, biomeColors)
	
	public fun getTopblock(biome: Biome): Material {
		return biomeTopblocks.get(biome) ?: Material.GRASS_BLOCK
	}
}
