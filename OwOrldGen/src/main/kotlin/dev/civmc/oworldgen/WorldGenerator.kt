package dev.civmc.oworldgen

import dev.civmc.oworldgen.populators.desert.DesertTreePopulator
import dev.civmc.oworldgen.populators.snow.SnowPopulator
import dev.civmc.oworldgen.populators.forest.Tree
import dev.civmc.oworldgen.populators.desert.DesertCactus
import dev.civmc.oworldgen.populators.desert.DesertDeadShrub
import dev.civmc.oworldgen.populators.forest.TallGrass
import dev.civmc.oworldgen.populators.snow.IceyWorm
import dev.civmc.oworldgen.populators.snow.SnowTree
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

class WorldGenerator(val config: ConfigurationSection): ChunkGenerator() {
    override fun getDefaultPopulators(world: World?): MutableList<BlockPopulator> {
        val generators = super.getDefaultPopulators(world)

        // forest
        generators.add(Tree(config.getConfigurationSection("populators.tree")))
        generators.add(TallGrass(config.getConfigurationSection("populators.tallGrass")))

        // desert
        generators.add(DesertTreePopulator(config.getConfigurationSection("populators.desertTree")))
        generators.add(DesertCactus(config.getConfigurationSection("populators.desertCactus")))
        generators.add(DesertDeadShrub())

        // snow
        generators.add(SnowTree(config.getConfigurationSection("populators.tree"))) // intentionally shared with tree
        generators.add(IceyWorm(this, config.getConfigurationSection("populators.iceWorm")))
        generators.add(SnowPopulator())

        return generators
    }

    val stoneLayerAvg = config.getInt("stoneLayer.avg")
    val stoneLayerMin = config.getInt("stoneLayer.min")
    val surfaceLayerHeight = config.getInt("surfaceLayer.height")
    val maxHeightModifier = config.getInt("sufaceLayer.maxModifier")

    val biomeBorderWaveyness = config.getInt("biomes.borderAmplitide")

    /**
     * The minumum "density" the terrain can have.
     *
     * This number will be expressed at the maximum reasonable distance.
     */
    val minimumThreshold = config.getDouble("density.min")

    /**
     * The maximum "density" the terrain can have.
     *
     * This number will be expressed at 0,0.
     */
    val maximumThreshold = config.getDouble("density.max")

    val minimumScale = config.getDouble("scale.min")
    val maximumScale = config.getDouble("scale.max")

    /**
     * The maximum distance a player is expected to explore:
     * The distance where terrain should stop increasing in "intensity", if possible.
     */
    val maximumReasonableDistance = config.getInt("maxReasonableDistance")

    val desertRadius = config.getInt("biomes.desertRadius")
    val tundraRadius = config.getInt("biomes.tundraRadius")

    val desertTransitionPeriod = desertRadius * config.getInt("biome.borderFrequency")
    val generateBedrock = config.getBoolean("generateBedrock")
    val onlyForest = config.getBoolean("biomes.onlyForest")

    override fun generateChunkData(world: World, random: Random, chunkX: Int, chunkZ: Int, biome: BiomeGrid): ChunkData {
        val chunk = createChunkData(world);
        val simplex = SimplexOctaveGenerator(Random(world.seed), 8)
        val worldHeightSimplex = SimplexNoiseGenerator(Random(world.seed xor 3))
        val stoneHeightSimplex = SimplexNoiseGenerator(Random(world.seed xor 4))

        for (x in 0..15) {
            val realX = chunkX * 16 + x

            for (z in 0..15) {
                val realZ = chunkZ * 16 + z
                val distanceFromCenter = distance(0.0, 0.0, realX.toDouble(), realZ.toDouble())

                var stoneLayerNoise = stoneHeightSimplex.noise(realX.toDouble() * 0.005, realZ.toDouble() * 0.005)
                stoneLayerNoise = (stoneLayerNoise + 1) / 2
                val stoneLayer = stoneLayerAvg * stoneLayerNoise + stoneLayerMin

                val topOfWorldGen = stoneLayer + surfaceLayerHeight
                val heightModifier = (topOfWorldGen +
                        distanceFade(realX, realZ, 0.0, maxHeightModifier.toDouble(), continueAfterReasonableDistance = true)) *
                        (worldHeightSimplex.noise(realX * 0.0005, realZ * 0.0005) + 1)


                val scale = distanceFade(realX, realZ, minimumScale, maximumScale, continueAfterReasonableDistance = true)
                simplex.setScale(scale)
                simplex.yScale = (scale / topOfWorldGen) * heightModifier


                val threshold = distanceFade(realX, realZ, maximumThreshold, minimumThreshold)

                for (y in 0..255) {
                    if (y <= stoneLayer) {
                        chunk.setBlock(x, y, z, Material.STONE)
                        continue
                    }

                    val realY = y;

                    val noise = simplex.noise(realX.toDouble(), realY.toDouble(), realZ.toDouble(), 0.0000001, 0.5, true)
                    val noiseModified = noise + (y / heightModifier)

                    if (noiseModified < threshold) {
                        chunk.setBlock(x, y, z, Material.STONE)
                    }
                }

                if (generateBedrock)
                    chunk.setBlock(x, 0, z, Material.BEDROCK)

                if (onlyForest) {
                    biome.setBiome(x, z, Biome.FOREST_HILLS)
                } else {
                    val theta = polarTheta(realX.toDouble(), realZ.toDouble())
                    val biomeNoise = simplex.noise(theta * (PI/180) * desertTransitionPeriod, 0.5, 0.5, true)
                    val modifier = biomeNoise * biomeBorderWaveyness

                    val desertRange = desertRadius + modifier
                    val tundraRange = tundraRadius + modifier

                    if (distanceFromCenter < desertRange)
                        biome.setBiome(x, z, Biome.FOREST_HILLS)
                    else if (distanceFromCenter < tundraRange)
                        biome.setBiome(x, z, Biome.DESERT_HILLS)
                    else
                        biome.setBiome(x, z, Biome.TAIGA_COLD_HILLS)
                }
            }
        }

        grassPostProcess(chunk, biome)

        return chunk;
    }

    val desertTopMaterial = Material.matchMaterial(config.getString("biomes.materials.desert.top"))
    val desertMidMaterial = Material.matchMaterial(config.getString("biomes.materials.desert.mid"))
    val forestTopMaterial = Material.matchMaterial(config.getString("biomes.materials.forest.top"))
    val forestMidMaterial = Material.matchMaterial(config.getString("biomes.materials.desert.mid"))

    fun grassPostProcess(chunk: ChunkData, biome: BiomeGrid) {
        for (x in 0..15) {
            for (z in 0..15) {
                val biomeXZ = biome.getBiome(x, z)

                val topMaterial = if (biomeXZ == Biome.DESERT_HILLS) desertTopMaterial else forestTopMaterial
                val fillerMaterial = if (biomeXZ == Biome.DESERT_HILLS) desertMidMaterial else forestMidMaterial
                val deepness = if (biomeXZ == Biome.DESERT_HILLS) 7 else 5

                for (y in 0..255) {
                    if (chunk.getType(x, y, z) == Material.AIR)
                        continue

                    if (chunk.getType(x, y, z) == Material.BEDROCK)
                        continue

                    if (chunk.getType(x, y + 1, z) == Material.AIR)
                        chunk.setBlock(x, y, z, topMaterial)
                    else for (yAbove in y..(y + deepness)) {
                        if (chunk.getType(x, yAbove, z) == Material.AIR)
                            chunk.setBlock(x, y, z, fillerMaterial)
                    }
                }
            }
        }
    }

    fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1))
    }

    fun polarTheta(x: Double, y: Double): Double {
        if (x == 0.0 && y == 0.0)
            return 0.0

        return atan(x / y)
    }

    fun zeroIfNegative(x: Double): Double {
        if (x < 0)
            return 0.0
        return x
    }

    fun distanceFade(x: Int, z: Int, min: Double, max: Double,
                     continueAfterReasonableDistance: Boolean = false,
                     maxDistance: Int = maximumReasonableDistance,
                     minDistance: Int = 0): Double {
        @Suppress("NAME_SHADOWING")
        var maxDistance = maxDistance

        var distance = distance(0.0, 0.0, x.toDouble(), z.toDouble())

        if (distance < minDistance) {
            return min
        } else if (minDistance != 0) {
            distance -= minDistance
            maxDistance -= minDistance
        }

        val difference = max - min
        val direction = signum(difference)

        val changePerBlock = difference / maxDistance

        var faded = distance * changePerBlock + min

        if (!continueAfterReasonableDistance) {
            if (direction < 0 && faded > min) {
                faded = min
            } else if (faded > max) {
                faded = max
            }
        }

        return faded
    }
}

