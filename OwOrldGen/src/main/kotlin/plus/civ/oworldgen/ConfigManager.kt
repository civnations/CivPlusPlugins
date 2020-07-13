package plus.civ.oworldgen

import org.bukkit.block.Biome
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.Color

object ConfigManager {
	private val mapImage: BufferedImage = ImageIO.read(File(OwOrldGen.instance.dataFolder, "biome_map.png"))
	private val biomeColors = HashMap<Int, Biome>()
	init {
		biomeColors[Color.RED.rgb] = Biome.DESERT
		biomeColors[Color.BLUE.rgb] = Biome.OCEAN
		biomeColors[Color.BLACK.rgb] = Biome.DEEP_OCEAN
		biomeColors[Color.GREEN.rgb] = Biome.RIVER
	}

	const val seed: Long = 853945834

	val mapImageWidth: Int
		get() = mapImage.width
	val mapImageHeight: Int
		get() = mapImage.height
	val radialBiomes: Array<Biome> = arrayOf(
			Biome.BADLANDS, Biome.BAMBOO_JUNGLE, Biome.BEACH, Biome.BIRCH_FOREST, Biome.DARK_FOREST, Biome.FOREST,
			Biome.TAIGA, Biome.JUNGLE, Biome.MOUNTAINS, Biome.MUSHROOM_FIELDS, Biome.PLAINS, Biome.SAVANNA, Biome.SWAMP
	)
	val biomeAlternatives = HashMap<Biome, Array<Biome>>()
	init {
		biomeAlternatives[Biome.BADLANDS] = arrayOf(Biome.ERODED_BADLANDS, Biome.BADLANDS_PLATEAU, Biome.WOODED_BADLANDS_PLATEAU)
		biomeAlternatives[Biome.BAMBOO_JUNGLE] = arrayOf(Biome.BAMBOO_JUNGLE_HILLS)
		biomeAlternatives[Biome.BIRCH_FOREST] = arrayOf(Biome.TALL_BIRCH_FOREST, Biome.TALL_BIRCH_HILLS, Biome.BIRCH_FOREST_HILLS)
		biomeAlternatives[Biome.DARK_FOREST] = arrayOf(Biome.DARK_FOREST_HILLS)
		biomeAlternatives[Biome.FOREST] = arrayOf(Biome.FLOWER_FOREST, Biome.WOODED_HILLS)
		biomeAlternatives[Biome.TAIGA] = arrayOf(
				Biome.TAIGA_HILLS, Biome.GIANT_SPRUCE_TAIGA, Biome.GIANT_TREE_TAIGA, Biome.TAIGA_MOUNTAINS, Biome.SNOWY_TAIGA_HILLS,
				Biome.SNOWY_TAIGA_MOUNTAINS, Biome.SNOWY_TAIGA, Biome.GIANT_SPRUCE_TAIGA_HILLS, Biome.GIANT_TREE_TAIGA_HILLS
		)
		biomeAlternatives[Biome.JUNGLE] = arrayOf(Biome.JUNGLE_HILLS)
		biomeAlternatives[Biome.MOUNTAINS] = arrayOf(Biome.GRAVELLY_MOUNTAINS, Biome.WOODED_MOUNTAINS)
		biomeAlternatives[Biome.PLAINS] = arrayOf(Biome.SUNFLOWER_PLAINS)
		biomeAlternatives[Biome.SAVANNA] = arrayOf(Biome.SAVANNA_PLATEAU, Biome.SHATTERED_SAVANNA_PLATEAU, Biome.SHATTERED_SAVANNA)
		biomeAlternatives[Biome.SWAMP] = arrayOf(Biome.SWAMP_HILLS)
		biomeAlternatives[Biome.DESERT] = arrayOf(Biome.DESERT_HILLS, Biome.DESERT_LAKES)
	}

	// Returns null if the biome should be filled in by angle, only returns ocean, desert or deep_ocean otherwise
	fun biomeAt(x: Int, z: Int): Biome? {
		if (x < 0 || x >= mapImage.width || z < 0 || z >= mapImage.height) {
			return Biome.DEEP_OCEAN
		}
		val mapColorThere: Int = mapImage.getRGB(x, z)
		return biomeColors[mapColorThere]
	}
}
