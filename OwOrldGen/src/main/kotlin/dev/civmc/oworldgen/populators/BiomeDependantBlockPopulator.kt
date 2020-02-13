package dev.civmc.oworldgen.populators

import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.generator.BlockPopulator

abstract class BiomeDependantBlockPopulator(val biome: Biome): BlockPopulator() {
    fun shouldRun(world: World, chunk: Chunk, x: Int, z: Int): Boolean {
        val bio = world.getBiome(chunk.x * 16 + x, chunk.z * 16 + z)

        return bio == biome
    }
}