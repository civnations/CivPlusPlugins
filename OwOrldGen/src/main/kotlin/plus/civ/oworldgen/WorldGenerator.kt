package plus.civ.oworldgen

import org.bukkit.World
import org.bukkit.generator.ChunkGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.*
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.generator.BlockPopulator
import org.bukkit.util.noise.SimplexNoiseGenerator
import java.lang.Math.*
import kotlin.math.atan
import org.bukkit.util.noise.SimplexNoiseGenerator

class WorldGenerator(): ChunkGenerator() {
	companion object {
		// offsets determining where the noise is sampled from
		private val fineOffset = 1000
		private val hillOffset = 5000
		private val broadOffset = 15000
		// amplitudes (the numerator is the amplitude in blocks)
		private val fineAmp = 2.0 / 255.0
		private val hillAmp = 15.0 / 255.0
		private val broadAmp = 35.0 / 255.0
		// scales
		private val fineScale = 0.1
		private val hillScale = 0.5
		private val broadScale = 2.0
	}
	
	private val mapdata = OwOrldGen.configManager.mapdata
	private val noise = SimplexNoiseGenerator.instance

	// returns an amount of blocks to shift the height at the given point
	fun noiseAt(x: Double, z: Double, fineCoeff: Double, hillCoeff: Double, broadCoeff: Double): Double {
		val fine = fineAmp * fineCoeff * (noise.noise(offset + x * fineScale, offset + z * fineScale) - 0.5) * 2
		val hill = hillAmp * hillCoeff * (noise.noise(offset + x * hillScale, offset + z * hillScale) - 0.5) * 2
		val broad = broadAmp * broadCoeff * (noise.noise(offset + x * broadScale, offset + z * broadScale) - 0.5) * 2
		val combined = broad + hill + fine
		return combined.clamp(-1.0, 1.0) * 255
	}
	
	fun biomeAt(x: Int, z: Int): Biome {
		return mapdata.getBiomeAt(x, z)
	}

	override fun generateChunkData(world: World, random: Random, chunkX: Int, chunkZ: Int, biomeGrid: BiomeGrid): ChunkData {
		val chunk = createChunkData(world)

		val seaLevel = OwOrldGen.configManager.seaLevel
		// how high the layer of dirt or whatever on top of the ground is
		val groundPadding = OwOrldGen.configManager.groundPadding
		
		for (x in 0..15) {
			val realX = chunkX * 16 + x
			for (z in 0..15) {
				val realZ = chunkZ * 16 + z

				val biome = biomeAt(realX, realZ)
				val genericHeight = mapdata.getHeightAt(realX, realZ)
				val heightShift = noiseAt(realX, realZ, mapdata.fineCoeffOf(biome), mapdata.hillCoeffOf(biome), mapdata.broadCoeffOf(biome))
				val height = genericHeight + heightShift
				if (height < seaLevel) {
					biome = Biome.OCEAN
				} else if (height < seaLevel + 5) {
					biome = Biome.BEACH
				}
				
				for (y in 0..255) {
					biomeGrid.setBiome(x, y, z, biome)

					if (y == 0) {
						chunk.setBlock(x, y, z, Material.BEDROCK)
						continue
					}

					if (biome == Biome.OCEAN) {
						if (y <= seaLevel && y > height) {
							chunk.setBlock(x, y, z, Material.WATER)
						} else if (y <= height) {
							chunk.setBlock(x, y, z, Material.CLAY)
						}
					} else if (biome == Biome.BEACH) {
						if (y <= height && y > height - groundPadding) {
							chunk.setBlock(x, y, z, Material.SAND)
						} else if (y <= height) {
							chunk.setBlock(x, y, z, Material.STONE)
						}
					} else {
						val topMaterial = OwOrldGen.configManager.getTopblock(biome)
						val underMaterial = Material.STONE
						if (y <= height && y >= height - groundPadding) {
							chunk.setBlock(x, y, z, topMaterial)
						} else if (y <= height) {
							chunk.setBlock(x, y, z, underMaterial)
						} else {
							chunk.setBlock(x, y, z, Material.AIR)
						}
					}
				}
			}
		}
		
		return chunk
	}
}
