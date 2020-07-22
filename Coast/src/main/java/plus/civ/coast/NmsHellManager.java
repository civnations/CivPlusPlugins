package plus.civ.coast;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that handles fast NMS modifications of blocks, and bulk sending of block modification packets.
 * Can theoretically have multiple instances (though that's not tested), but should generally be treated like a singleton owned by the main Coast instance.
 */
public class NmsHellManager {

	// A list of block modifications to send packets about later
	// Concurrent in case we ever want to do some multithreading for optimization
	// SkipListSet because we need it to stay sorted by chunk, since packets must be batched by chunk
	private final ConcurrentHashMap<ChunkCoordIntPair, List<Location>> modificationsToSend;

	public NmsHellManager() {
		this.modificationsToSend = new ConcurrentHashMap<>();
	}

	/**
	 * Sets a block's type very quickly, by cutting some corners and skipping the part where packets are sent to players.
	 * After you're done using this (in multiple calls), call sendQueuedBlockChanges() to actually send the packets in a batch.
	 * @param l The location of the block to set
	 * @param mat What to set the block to
	 */
	public void setBlockQuickly(Location l, Block mat) {
		Chunk chunk = ((CraftChunk)l.getChunk()).getHandle();
		int i = l.getBlockX();
		int j = l.getBlockY();
		int k = l.getBlockZ();
		// j >> 4 is j / 16, but I'm not changing it because I'm unsure if it's exactly the same or if there's slight difference (NMS does it this way but could be due to compiler optimization)
		ChunkSection chunkSection = chunk.getSections()[j >> 4];
		IBlockData matBlockData = mat.getBlockData();

		// if chunkSection is not generated, we convince Minecraft to generate it by setting this block in it
		if (chunkSection == null) {
			// From what I can tell, flag is unused, and doPlace is whether you want to do onPlace logic (ie surrounding block-updates)
			chunk.setType(new BlockPosition(i, j, k), matBlockData, false, false);
		} else {
			// Here, I can't totally tell because everything is very obfuscated, but I think the false at the end tells it not to bother with updating surrounding blocks
			// i & 15 is the same as i % 15, optimized. The compiler probably would do this optimization but I found the code like this so will leave it
			chunkSection.setType(i & 15, j & 15, k & 15, matBlockData, false);
		}

		ChunkCoordIntPair chunkLoc = chunk.getPos();
		List<Location> chunkModsList = modificationsToSend.get(chunkLoc);
		if (chunkModsList != null) {
			chunkModsList.add(l);
		} else {
			chunkModsList = Collections.synchronizedList(new ArrayList<Location>());
			chunkModsList.add(l);
			modificationsToSend.put(chunkLoc, chunkModsList);
		}
	}

	public void sendQueuedBlockChanges() {
		// Iterate through all of modificationsToSend
		modificationsToSend.forEachEntry(6, e -> {
			List<Location> modifications = e.getValue();
			Chunk chunk = ((CraftChunk)modifications.get(0).getChunk()).getHandle();

			sendPacket(createMultiBlockChangePacket(modifications, chunk), chunk);

			// Sent, so we remove it from the list
			modificationsToSend.remove(e.getKey());
		});
	}

	private void sendPacket(PacketPlayOutMultiBlockChange packet, Chunk chunk) {
		for (EntityPlayer p : (Iterable<EntityPlayer>)((((WorldServer)chunk.getWorld()).getChunkProvider().playerChunkMap.visibleChunks.get(chunk.getPos().pair()).players.a(chunk.getPos(), false))::iterator)) {
			p.playerConnection.sendPacket(packet);
		}
		chunk.markDirty();
	}

	// TODO sometimes, this includes duplicate locations. I'm unsure if that's actually a problem. If it is, maybe remove duplicate values before making the packet
	private PacketPlayOutMultiBlockChange createMultiBlockChangePacket(List<Location> modifications, Chunk chunk) {
		int size = modifications.size();
		short[] data = new short[size];
		for (int i = 0; i < size; i++) {
			Location l = modifications.get(i);

			int x = l.getBlockX();
			int y = l.getBlockY();
			int z = l.getBlockZ();
			data[i] = (short)((y) | (((x) & 15) << 12) | (((z) & 15) << 8));
		}
		return new PacketPlayOutMultiBlockChange(size, data, chunk);
	}
}
