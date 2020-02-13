package dev.civmc.oworldgen.populators.snow

import dev.civmc.oworldgen.WorldGenerator
import dev.civmc.oworldgen.populators.BiomeDependantBlockPopulator
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.*

class IceyWorm(val generator: WorldGenerator, config: ConfigurationSection): BiomeDependantBlockPopulator(Biome.TAIGA_COLD_HILLS) {
    val scale = config.getDouble("scale")
    val maxThreshold = config.getDouble("maxThreshold")
    val minthreshold = config.getDouble("minThreshold")

    override fun populate(world: World, random: Random, chunk: Chunk) {
        val simplex = SimplexOctaveGenerator(Random(world.seed xor 1), 8)
        simplex.setScale(scale)

        for (x in 0..15) {
            val realX = chunk.x * 16 + x
            for (z in 0..15) {
                val realZ = chunk.z * 16 + z

                if (!shouldRun(world, chunk, x, z))
                    continue

                for (y in 0..255) {
                    val realY = y

                    if (chunk.getBlock(x, y, z).type == Material.AIR)
                        continue

                    val threshold = generator.distanceFade(realX, realZ, maxThreshold, minthreshold, minDistance = generator.tundraRadius)
                    val noise = simplex.noise(realX.toDouble(), realY.toDouble(), realZ.toDouble(), 0.5, 0.5, true)

                    if (noise > threshold) {
                        chunk.getBlock(x, y, z).type = Material.ICE
                    }
                }
            }
        }
    }
}