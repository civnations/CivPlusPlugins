package dev.civmc.oworldgen.populators.desert

import dev.civmc.oworldgen.populators.BiomeDependantBlockPopulator
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.configuration.ConfigurationSection
import java.util.*

class DesertCactus(config: ConfigurationSection): BiomeDependantBlockPopulator(Biome.DESERT_HILLS) {
    val spawnChance = 0.005

    override fun populate(world: World, random: Random, chunk: Chunk) {
        for (x in 0..15) {
            for (z in 0..15) {
                if (!shouldRun(world, chunk, x, z))
                    continue

                y@for (y in 0..255) {
                    if (chunk.getBlock(x, y - 1, z).type != Material.SAND)
                        continue

                    if (random.nextFloat() > spawnChance)
                        continue

                    for (checkY in y..(y+2)) {
                        if (chunk.getBlock(x, checkY, z).type != Material.AIR)
                            continue@y
                    }

                    for (placeY in y..(y+2)) {
                        chunk.getBlock(x, placeY, z).type = Material.CACTUS
                    }
                }
            }
        }
    }
}