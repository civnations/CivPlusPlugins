package dev.civmc.oworldgen.populators.desert

import dev.civmc.oworldgen.populators.BiomeDependantBlockPopulator
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import java.util.*

class DesertDeadShrub: BiomeDependantBlockPopulator(Biome.DESERT_HILLS) {
    override fun populate(world: World, random: Random, chunk: Chunk) {
        for (x in 0..15) {
            for (z in 0..15) {
                if (!shouldRun(world, chunk, x, z))
                    continue

                for (y in 0..255) {
                    if (chunk.getBlock(x, y - 1, z).type != Material.SAND)
                        continue

                    if (chunk.getBlock(x, y, z).type != Material.AIR)
                        continue

                    if (random.nextFloat() > 0.003)
                        continue

                    chunk.getBlock(x, y, z).type = Material.DEAD_BUSH
                }
            }
        }
    }
}