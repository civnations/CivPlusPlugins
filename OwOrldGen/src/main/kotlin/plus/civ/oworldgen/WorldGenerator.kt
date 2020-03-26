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

class WorldGenerator(): ChunkGenerator() {

	val mapdata = OwOrldGen.configManager.mapdata

	override fun generateChunkData(world: World, random: Random, chunkX: Int, chunkZ: Int, biomeGrid: BiomeGrid): ChunkData {
		val chunk = createChunkData(world)

		val seaLevel = OwOrldGen.configManager.seaLevel
		// how high the layer of dirt on top of the ground is
		val groundPadding = OwOrldGen.configManager.groundPadding
		for (x in 0..15) {
			val realX = chunkX * 16 + x
			for (z in 0..15) {
				val realZ = chunkZ * 16 + z

				// this may be changed to beach later sometimes, if it's ocean
				val biome = mapdata.getBiomeAt(realX, realZ)
				
				// we're not generating anything in deep ocean
				if (biome == Biome.DEEP_OCEAN) {
					continue
				}
				
				val height = mapdata.getHeightAt(realX, realZ)
				for (y in 0..255) {
					biomeGrid.setBiome(x, y, z, biome)

					if (y == 0) {
						chunk.setBlock(x, y, z, Material.BEDROCK)
						continue
					}

					if (biome == Biome.OCEAN) {
						if (height >= seaLevel) {
							if (y <= height && y > height - groundPadding) {
								chunk.setBlock(x, y, z, Material.SAND)
							} else if (y <= height) {
								chunk.setBlock(x, y, z, Material.STONE)
							}
							biomeGrid.setBiome(x, y, z, Biome.BEACH)
						} else {
							if (y <= seaLevel && y > height) {
								chunk.setBlock(x, y, z, Material.WATER)
							} else if (y <= height) {
								chunk.setBlock(x, y, z, Material.CLAY)
							}
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
