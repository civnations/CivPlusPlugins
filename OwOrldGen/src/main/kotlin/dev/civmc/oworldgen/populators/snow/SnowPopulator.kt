package dev.civmc.oworldgen.populators.snow

import dev.civmc.oworldgen.populators.BiomeDependantBlockPopulator
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import java.util.*

class SnowPopulator: BiomeDependantBlockPopulator(Biome.TAIGA_COLD_HILLS) {
    override fun populate(world: World, random: Random, chunk: Chunk) {
       for (x in 0..15) {
           xy@for (z in 0..15) {
               if (!shouldRun(world, chunk, x, z))
                   continue

               for (y in 255 downTo 0) {
                   if (chunk.getBlock(x, y - 1, z).type == Material.ICE)
                       continue@xy

                   if (chunk.getBlock(x, y - 1, z).type != Material.AIR) {
                       chunk.getBlock(x, y, z).type = Material.SNOW
                       continue@xy
                   }
               }
           }
       }
    }
}