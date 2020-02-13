package dev.civmc.oworldgen.populators.forest

import dev.civmc.oworldgen.WorldGenerator
import dev.civmc.oworldgen.populators.BiomeDependantBlockPopulator
import org.bukkit.*
import org.bukkit.block.Biome
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.noise.SimplexNoiseGenerator
import java.util.*
import kotlin.math.max

class Tree(config: ConfigurationSection): BiomeDependantBlockPopulator(Biome.FOREST_HILLS) {
    val treeBiomeScale = config.getDouble("biomeScale")
    val treeSpacing = config.getDouble("treeSpacing")
    val treeThreshold = config.getDouble("treeThreshold")

    override fun populate(world: World, random: Random, chunk: Chunk) {
        val oakSomplex = SimplexNoiseGenerator(Random(world.seed xor 5))
        val birchSomplex = SimplexNoiseGenerator(Random(world.seed xor 7))
        val noneSimplex = SimplexNoiseGenerator(Random(world.seed xor 9))

        val individualTreeSimplex = SimplexNoiseGenerator(Random(world.seed xor 8))

        for (x in 0..15) {
            val realX = chunk.x * 16 + x
            for (z in 0..15) {
                val realZ = chunk.z * 16 + z
                for (y in 0..255) {
                    val realY = y

                    if (chunk.getBlock(x, y - 1, z).type != Material.GRASS)
                        continue

                    val individualTreeNoise = individualTreeSimplex.noise(realX * treeSpacing, realY * treeSpacing, realZ * treeSpacing)
                    if (individualTreeNoise < treeThreshold)
                        continue

                    val oakNoise = oakSomplex.noise(realX * treeBiomeScale, realZ * treeBiomeScale)
                    val oakRandom = random.nextFloat()
                    val birchNoise = birchSomplex.noise(realX * treeBiomeScale, realZ * treeBiomeScale)
                    val birchRandom = random.nextFloat()
                    val noneNoise = noneSimplex.noise(realX * treeBiomeScale, realZ * treeBiomeScale)
                    val noneRandom = random.nextFloat()

                    val oakValue = oakNoise * oakRandom
                    val birchValue = birchNoise * birchRandom
                    val noneValue = noneNoise * noneRandom

                    val max = max(oakValue, max(birchValue, noneValue))

                    var treeType: TreeType? = null

                    when (max) {
                        oakValue -> treeType = TreeType.TREE
                        birchValue -> treeType = TreeType.BIRCH
                        noneValue -> treeType = null
                    }

                    if (treeType != null)
                        world.generateTree(Location(world, realX.toDouble(), y.toDouble(), realZ.toDouble()), treeType)
                }

            }
        }
    }

}