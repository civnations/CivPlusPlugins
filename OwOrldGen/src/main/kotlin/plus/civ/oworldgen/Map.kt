package plus.civ.oworldgen

import org.bukkit.block.Biome
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class Map(biomeImageFilePath: String, heightImageFilePath: String, private val biomeColors: HashMap<Color, Biome>) {
    private val biomeImage: BufferedImage = ImageIO.read(File(OwOrldGen.instance.dataFolder, biomeImageFilePath))
	private val heightImage: BufferedImage = ImageIO.read(File(OwOrldGen.instance.dataFolder, heightImageFilePath))

    public fun getBiomeAt(x: Int, z: Int): Biome {
        if (x < 0 || x >= biomeImage.width || z < 0 || z >= biomeImage.height) {
            return Biome.DEEP_OCEAN
        }
		
        val rgbInt: Int = biomeImage.getRGB(x, z)
        val color: Color = Color(rgbInt)
        val biome: Biome = closestBiome(color)
        return biome
    }
	
	public fun getHeightAt(x: Int, z: Int): Int {
		if (x < 0 || x >= heightImage.width || z < 0 || z >= heightImage.height) {
            return 0
        }
		
		val rgbInt: Int = heightImage.getRGB(x, z)
		val color: Color = Color(rgbInt)
		val colorBrightness: Float = color.getRed() / 255.0f
		// TODO magic numbers should be config (5 is currently the lowest terrain gets and 175 is highest)
		return (5 + colorBrightness * (170)).toInt()
	}
	
	private fun closestBiome(color: Color): Biome {
		val biomeMaybe = biomeColors.get(color)
		if (biomeMaybe != null) {
			return biomeMaybe
		}
		
		var closestDist = 255 * 4
		var closestBiome: Biome = Biome.OCEAN
		for ((key, value) in biomeColors) {
			val distRed = Math.abs(color.getRed() - key.getRed())
			val distGreen = Math.abs(color.getGreen() - key.getGreen())
			val distBlue = Math.abs(color.getBlue() - key.getBlue())
			val distSum = distRed + distGreen + distBlue
			if (distSum < closestDist) {
				closestDist = distSum
				closestBiome = value
			}
		}
		return closestBiome
	}
}
