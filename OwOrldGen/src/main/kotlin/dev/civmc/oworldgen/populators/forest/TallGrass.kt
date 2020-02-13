package dev.civmc.oworldgen.populators.forest

import dev.civmc.oworldgen.WorldGenerator
import dev.civmc.oworldgen.populators.BiomeDependantBlockPopulator
import org.bukkit.Chunk
import org.bukkit.GrassSpecies
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.material.LongGrass
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.*

class TallGrass(config: ConfigurationSection) : BiomeDependantBlockPopulator(Biome.FOREST_HILLS) {
    val noiseScale = config.getDouble("scale")

    override fun populate(world: World, random: Random, chunk: Chunk) {
        val simplex = SimplexOctaveGenerator(Random(world.seed xor 2), 8)
        simplex.setScale(noiseScale)

        for (x in 0..15) {
            val realX = chunk.x * 16 + x

            for (z in 0..15) {
                val realZ = chunk.z * 16 + z

                if (!shouldRun(world, chunk, x, z))
                    continue

                for (y in 0..255) {
                    val realY = y

                    if (chunk.getBlock(x, y - 1, z).type != Material.GRASS)
                        continue

                    if (chunk.getBlock(x, y, z).type != Material.AIR)
                        continue

                    val noise = simplex.noise(realX.toDouble(), realY.toDouble(), realZ.toDouble(), 0.5, 0.5, true)

                    if (random.nextFloat() > noise)
                        continue

                    chunk.getBlock(x, y, z).type = Material.LONG_GRASS
                    val state = chunk.getBlock(x, y, z).state
                    val data = state.data
                    if (data is LongGrass) {
                        data.species = GrassSpecies.NORMAL
                    }
                    state.data = data
                    state.update()
                }
            }
        }
    }
}