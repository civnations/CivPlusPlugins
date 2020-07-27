package plus.civ.coast;

import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.Blocks;

public class Config {
	// Blocks that a meteor is made of. You can add duplicates to mess with ratios
	public static final Block[] METEOR_BLOCKS = {Blocks.OBSIDIAN, Blocks.OBSIDIAN, Blocks.NETHERRACK, Blocks.MAGMA_BLOCK, Blocks.MAGMA_BLOCK};
	// How many ticks to wait between updating a meteor
	public static final long TICK_RATE = 3;
	// Default time in minutes to wait before summoning a meteor
	public static final float DEFAULT_DELAY = 0.25f;
	// Default speed of meteors if no speed is provided
	public static final float DEFAULT_SPEED = 3.0f;
	// Default radius of meteors if no other radius is provided
	public static final float DEFAULT_RADIUS = 2.5f;
	// The factor of velocity a meteor loses for every collision, proportional to its volume (eg this will be lower for bigger meteors)
	public static final float INERTIA_DECAY = 3.0f;
	// Factor that explosion power gets multiplied by for the explosions when meteors collide with things
	public static final float EXPLOSION_POWER_FACTOR = 0.75f;
}
