package plus.civ.coast;

import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.Blocks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Meteor extends BukkitRunnable {
	private final NmsHellManager nmsHellManager;

	// A blueprint of what the meteor looks like
    private final Block[][][] blocks;
	// The factor of the meteor's velocity subtracted whenever it collides with a block
	private final float inertiaDecay;
    // The world the meteor is in
	private final World world;
	// Radius of meteor
	private final float radius;
	// The inventory to be placed in the meteor's chest after it lands
	private final Inventory inv;
	// The dx, dy, and dz that we started with, but normalized and multiplied by radius. Later used to place explosions in front of the meteor
	private final float frontX, frontY, frontZ;

    // Current position
    protected float x, y, z;
    // Velocities (per update)
    private float dx, dy, dz;

    // We're gonna search this a lot
    private ArrayList<Location> blockChangesLastFrame = new ArrayList<Location>();

    /**
     * Create a new meteor. NOTE: It is bad to use this directly. Instead, you should usually use Coast.summonMeteor()
     * @param x The center x coordinate
     * @param y The center y coordinate
     * @param z The center z coordinate
     * @param pitch The pitch of the meteor's velocity
     * @param yaw The yaw of the meteor's velocity
     * @param speed The meteor's speed (magnitude of velocity)
     * @param radius The radius of the meteor in blocks (decimals can be used to mess with the exact shape)
	 * @param inv The Inventory that will be put in the meteor's chest after it lands
     */
    public Meteor(World world, float x, float y, float z, float pitch, float yaw, float speed, float radius, Inventory inv) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.radius = radius;
		this.world = world;
		this.nmsHellManager = Coast.getInstance().getNmsHellManager();
		this.inv = inv;
		// Could calculate this on the fly but we cache it because it needs to be used many times per tick
		// The formula used makes it so that all spheres should get about equally deep into the ground (basically dividing by volume)
		this.inertiaDecay = Config.INERTIA_DECAY / ((float)Math.PI * radius * radius * radius);
		// Calculate dx, dy, dz
		double dxPreNormalized = Math.cos(pitch) * Math.sin(-yaw);
		double dyPreNormalized = Math.sin(-pitch);
		double dzPreNormalized = Math.cos(pitch) * Math.cos(yaw);
		double magnitude = Math.sqrt(Math.pow(dxPreNormalized, 2) + Math.pow(dyPreNormalized, 2) + Math.pow(dzPreNormalized, 2));
		double dxNormed = dxPreNormalized / magnitude;
		double dyNormed = dyPreNormalized / magnitude;
		double dzNormed = dzPreNormalized / magnitude;
		this.dx = (float)(speed * dxNormed);
		this.dy = (float)(speed * dyNormed);
		this.dz = (float)(speed * dzNormed);
		this.frontX = (float)((radius + 2) * dxNormed);
		this.frontY = (float)((radius + 2) * dyNormed);
		this.frontZ = (float)((radius + 2) * dzNormed);

        // Initialize blocks array
        int blocksArrayWidth = (int)Math.ceil(radius * 2);
        this.blocks = new Block[blocksArrayWidth][blocksArrayWidth][blocksArrayWidth];

        // Fill blocks array to make a spherical meteor
        for (int i = 0; i < blocksArrayWidth; i++) {
            for (int j = 0; j < blocksArrayWidth; j++) {
                for (int k = 0; k < blocksArrayWidth; k++) {
                    // Calculate distance from center. If it's > radius, we're outside the sphere, otherwise, inside. Set mat accordingly.
                    double distFromCenter = distance3f(i, j, k, radius, radius, radius);
                    if (distFromCenter < radius) {
                        // this could be sped up a little bit if
                        blocks[i][j][k] = selectRandomMeteorMaterial();
                    } else {
                        blocks[i][j][k] = Blocks.AIR;
                    }
                }
            }
        }

        // We don't need to actually set the blocks yet, because that'll just happen when the runnable is first called (which might be much later, if there's supposed to be a delay).
    }

    // Called every update
	@Override
	public void run() {
		// Move the meteor
    	this.x += dx;
		this.y += dy;
		this.z += dz;

		// kill the meteor if it goes too high in the air, or too low underground
		if (this.y + this.radius * 2 >= 255 || this.y - radius * 2 <= 0) {
			this.touchdownAndCleanup();
			return;
		}

		// Spawn pretty particles behind the meteor
		Location particleLoc = new Location(this.world, this.x - this.frontX, this.y - this.frontY, this.z - this.frontZ);
		Location particleLocTwo = new Location(this.world, this.x - this.frontX * 2, this.y - this.frontY * 2, this.z - this.frontZ * 2);
		Location particleLocThree = new Location(this.world, this.x - this.frontX * 3, this.y - this.frontY * 3, this.z - this.frontZ * 3);
		this.world.spawnParticle(Particle.FLAME, particleLoc, 32);
		this.world.spawnParticle(Particle.DRAGON_BREATH, particleLocTwo, 32);
		this.world.spawnParticle(Particle.SMOKE_LARGE, particleLocThree, 32);

		// Set the new blocks, get rid of the old blocks
		int collides = setCurrentBlocksAndRemoveOldBlocks();

		// Reduce velocity based on the amount of collisions
		dx *= (1.0f - Math.min(1.0, collides * this.inertiaDecay));
		dy *= (1.0f - Math.min(1.0, collides * this.inertiaDecay));
		dz *= (1.0f - Math.min(1.0, collides * this.inertiaDecay));
		// If velocity is now quite low, it's time to finish up
		double magnitude = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2) + Math.pow(dz, 2));
		if (magnitude <= 0.5) {
			this.touchdownAndCleanup();
			return;
		}

		// Make an explosions depending on how many collides there were
		if (collides > 0) {
			Location explosionLoc = new Location(this.world, this.x + this.frontX, this.y + this.frontY, this.z + this.frontZ);
			final float explosionPower = (float)Math.sqrt(collides) * Config.EXPLOSION_POWER_FACTOR;
			this.world.createExplosion(explosionLoc, explosionPower, true, true);
		}
	}

	/**
	 * Sets current blocks of the meteor and unsets the last batch
	 * @return The amount of blocks that had to be overwritten that were not air
	 */
	private int setCurrentBlocksAndRemoveOldBlocks() {
    	// List of any locations that we change blocks at
		ArrayList<Location> newBlockChanges = new ArrayList<Location>();
		// The amount of blocks that the meteor is colliding with this frame
		int collisions = 0;

		// nReal is the coordinate within the world of the starting corner, and nFake is the coordinate within this.blocks
		int xReal = (int)this.x - this.blocks.length / 2;
		int yReal = (int)this.y - this.blocks[0].length / 2;
		int zReal = (int)this.z - this.blocks[0][0].length / 2;
		// For all blocks in the meteor's current hitbox
		for (int xFake = 0; xFake < this.blocks.length; xFake++) {
			for (int yFake = 0; yFake < this.blocks[0].length; yFake++) {
				for (int zFake = 0; zFake < this.blocks[0][0].length; zFake++) {
					Location loc = new Location(world,xReal + xFake, yReal + yFake, zReal + zFake);
					Block b = this.blocks[xFake][yFake][zFake];
					boolean airAdjacent = this.isAirAdjacent(xFake, yFake, zFake);

					if (b != Blocks.AIR) {
						// if we're colliding with something here (ie if there's already a block in the path)
						if (world.getBlockAt(loc).getType() != Material.AIR) {
							// nested if instead of an && because this check is quite expensive, but necessary to avoid the meteor colliding with itself
							if (!blockChangesLastFrame.contains(loc)) {
								collisions++;
								// TODO Sometimes, make an explosion
								// we maybe weren't planning on changing that one normally, but need to in this case to kill the old block
								this.nmsHellManager.setBlockQuickly(loc, b);
								newBlockChanges.add(loc);
							}
						// We skip non-air-adjacent blocks because they can't be seen anyway
						} else if (airAdjacent) {
							this.nmsHellManager.setBlockQuickly(loc, b);
							newBlockChanges.add(loc);
						}
					}
				}
			}
		}

		// Get the set representing the blocks that were in changesLastFrame and are not in newBlockChanges
		// We can do this destructively because we're about to throw out the lastFrame changes list anyway
		if (!this.blockChangesLastFrame.isEmpty()) {
			this.blockChangesLastFrame.removeAll(newBlockChanges);
			// Change all of those to air
			for (Location l : blockChangesLastFrame) {
				this.nmsHellManager.setBlockQuickly(l, Blocks.AIR);
			}
		}
		// changesLastFrame becomes newBlockChanges (to be used next frame)
		this.blockChangesLastFrame = newBlockChanges;
		// send packets representing the block changes that were just made
		this.nmsHellManager.sendQueuedBlockChanges();

		return collisions;
	}

	// Called when the meteor is done moving
	private void touchdownAndCleanup() {
		// One last time, change all the blocks, but this time don't skip hollow stuff
		int xReal = (int)this.x - this.blocks.length / 2;
		int yReal = (int)this.y - this.blocks[0].length / 2;
		int zReal = (int)this.z - this.blocks[0][0].length / 2;
		// For all blocks in the meteor's current hitbox
		for (int xFake = 0; xFake < this.blocks.length; xFake++) {
			for (int yFake = 0; yFake < this.blocks[0].length; yFake++) {
				for (int zFake = 0; zFake < this.blocks[0][0].length; zFake++) {
					Location loc = new Location(world,xReal + xFake, yReal + yFake, zReal + zFake);
					Block b = this.blocks[xFake][yFake][zFake];

					if (b != Blocks.AIR) {
						this.nmsHellManager.setBlockQuickly(loc, b);
					}
				}
			}
		}
		this.nmsHellManager.sendQueuedBlockChanges();

		// Set the chest in the middle of the meteor
		// Create chest in center of meteor
		Location chestLoc = new Location(world, (int)x, (int)y, (int)z);
		org.bukkit.block.Block chestBlock = chestLoc.getBlock();
		chestBlock.setType(Material.CHEST);
		Chest chest = ((Chest) chestBlock.getState());
		// Fill the chest
		for (ItemStack i : inv.getContents()) {
			if (i == null) {
				continue;
			}
			chest.getInventory().addItem(i);
		}

		// Cancel the Runnable, we're done
		this.cancel();
	}

	/**
	 * @param x The x coordinate local to the meteor (NOT worldspace)
	 * @param y The y coordinate local to the meteor (NOT worldspace)
	 * @param z The z coordinate local to the meteor (NOT worldspace)
	 * @return Whether the given block is adjacent to air, or is on a face of the meteor's hitbox
	 */
	private boolean isAirAdjacent(int x, int y, int z) {
    	boolean ret;
    	try {
			ret =
					this.blocks[x + 1][y + 0][z + 0] == Blocks.AIR ||
					this.blocks[x - 1][y + 0][z + 0] == Blocks.AIR ||
					this.blocks[x + 0][y + 1][z + 0] == Blocks.AIR ||
					this.blocks[x + 0][y - 1][z + 0] == Blocks.AIR ||
					this.blocks[x + 0][y + 0][z + 1] == Blocks.AIR ||
					this.blocks[x + 0][y + 0][z - 1] == Blocks.AIR;
		} catch (IndexOutOfBoundsException e) {
    		// If we get this exception, we're somewhere on the edge of the meteor's hitbox. In that case, we should always pretend we're air-adjacent
    		return true;
		}
    	return ret;
	}

    private static Block selectRandomMeteorMaterial() {
        int totalMats = Config.METEOR_BLOCKS.length;
        int selected = (int)(Math.random() * totalMats);
        return Config.METEOR_BLOCKS[selected];
    }

    private static float distance3f(float a, float b, float c, float x, float y, float z) {
		return (float)Math.sqrt(Math.pow(a - x, 2) + Math.pow(b - y, 2) + Math.pow(c - z, 2));
	}
}
