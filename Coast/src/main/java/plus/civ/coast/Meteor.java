package plus.civ.coast;

import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.Blocks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Meteor extends BukkitRunnable {
	private final NmsHellManager nmsHellManager;

	// A blueprint of what the meteor looks like
    private final Block[][][] blocks;

    // The world the meteor is in
	private World world;
    // Current position
    private float x, y, z;
    // Velocities (per update)
    private float dx, dy, dz;
    // Radius of meteor
	private float radius;

    // We're gonna search this a lot
    private ArrayList<Location> blockChangesLastFrame = new ArrayList<Location>();

    /**
     * Create a new meteor. NOTE: It is bad to use this directly. Instead, you should usually use Coast.summonMeteor()
     * @param x The center x coordinate
     * @param y The center y coordinate
     * @param z The center z coordinate
     * @param dx The x distance to move per in-game tick
     * @param dy The y distance to move per in-game tick
     * @param dz The z distance to move per in-game tick
     * @param radius The radius of the meteor in blocks (decimals can be used to mess with the exact shape)
     */
    public Meteor(World world, float x, float y, float z, float dx, float dy, float dz, float radius) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		this.radius = radius;
		this.world = world;
		this.nmsHellManager = Coast.getInstance().getNmsHellManager();

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

		// kill the meteor if it goes too high in the air
		if (this.y + this.radius * 2 >= 255) {
			this.touchdownAndCleanup();
			return;
		}

		// Set the new blocks, get rid of the old blocks
		int collides = setCurrentBlocksAndRemoveOldBlocks();

		// Reduce velocity based on the amount of collisions
		dx *= (1.0f - Math.min(1.0, collides * 0.03));
		dy *= (1.0f - Math.min(1.0, collides * 0.03));
		dz *= (1.0f - Math.min(1.0, collides * 0.03));
		// If velocity is now quite low, it's time to finish up
		double magnitude = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2) + Math.pow(dz, 2));
		if (magnitude <= 0.5) {
			this.touchdownAndCleanup();
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
		// TODO implement
		// For now, do nothing, just kill self
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
