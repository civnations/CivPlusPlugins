package plus.civ.oworldgen

import nl.rutgerkok.worldgeneratorapi.BiomeGenerator
import org.bukkit.block.Biome
import org.bukkit.util.noise.SimplexNoiseGenerator
import kotlin.math.PI
import kotlin.math.atan2

class RadialBiomeGenerator: BiomeGenerator {

	private val noise: SimplexNoiseGenerator = SimplexNoiseGenerator(ConfigManager.seed)

	// x, y, and z are the actual coordinate divided by 4, and rounded (I forget if up or down. probably down)
	override fun getZoomedOutBiome(unshiftedX: Int, y: Int, unshiftedZ: Int): Biome {
		val x = unshiftedX + ConfigManager.mapImageWidth / 2
		val z = unshiftedZ + ConfigManager.mapImageHeight / 2
		val biomeFromMapImage = ConfigManager.biomeAt(x, z)
		if (biomeFromMapImage != null) {
			return chooseAlternative(biomeFromMapImage, x, z)
		}

		val angle = atan2(z.toDouble(), x.toDouble()) + PI
		val circlePortion = angle / (2.0 * PI)
		val range = 1.0 / ConfigManager.radialBiomes.size
		var biomeIndex = (circlePortion / range).toInt()
		if (biomeIndex == ConfigManager.radialBiomes.size) {
			biomeIndex--
		}
		return chooseAlternative(ConfigManager.radialBiomes[biomeIndex], x, z)
	}

	private fun chooseAlternative(biome: Biome, x: Int, z: Int): Biome {
		val potentialAlternatives = ConfigManager.biomeAlternatives[biome] ?: return biome
		val range = 1.0 / (potentialAlternatives.size + 1)
		val roll = noiseAt(x, z)
		if (roll >= range * (potentialAlternatives.size - 1)) {
			return biome
		}
		return potentialAlternatives[(roll / range).toInt()]
	}

	private fun noiseAt(x: Int, z: Int): Double {
		val ret = this.noise.noise(x.toDouble() * 0.1, z.toDouble() * 0.1)
		if (ret < 0) {
			return 0.0
		}
		if (ret > 1) {
			return 1.0
		}
		return ret
	}
}
