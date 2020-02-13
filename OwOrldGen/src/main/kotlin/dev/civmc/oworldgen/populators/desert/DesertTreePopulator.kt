package dev.civmc.oworldgen.populators.desert

import dev.civmc.oworldgen.populators.BiomeDependantBlockPopulator
import org.bukkit.*
import org.bukkit.block.Biome
import org.bukkit.configuration.ConfigurationSection
import java.util.*

class DesertTreePopulator(config: ConfigurationSection): BiomeDependantBlockPopulator(Biome.DESERT_HILLS) {
    val treeSpawnChance = config.getDouble("spawnChance")

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

                    if (random.nextFloat() > treeSpawnChance)
                        continue

                    chunk.getBlock(x, y, z).type = Material.AIR

                    chunk.getBlock(x, y - 1, z).type = Material.GRASS
                    world.generateTree(Location(world,
                            (chunk.x * 16 + x).toDouble(),
                            y.toDouble(),
                            (chunk.z * 16 + z).toDouble()), TreeType.TREE)
                    chunk.getBlock(x, y - 1, z).type = Material.SAND
                }

            }
        }
    }
}