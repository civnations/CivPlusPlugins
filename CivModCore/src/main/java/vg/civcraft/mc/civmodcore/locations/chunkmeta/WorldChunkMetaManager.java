package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import org.bukkit.World;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Stores Chunk metadata for all plugins for one specific world. Metadata is
 * kept in a cache, into which is inserted when a chunk is loaded. When a chunk
 * is unloaded, it is placed in an unloading queue and will be unloaded after a
 * certain interval if it hasn't been reloaded in the mean time
 *
 */
public class WorldChunkMetaManager {

	/**
	 * How long should chunk data be kept in memory after the chunk is unloaded? 5
	 * minutes
	 */
	private static final long UNLOAD_DELAY = 5L * 60L * 1000L;
	private static final long UNLOAD_CHECK_INTERVAL = 5L * 60L * 1000L;

	private final short worldID;
	private final Map<ChunkCoord, ChunkCoord> metas;
	/**
	 * A synchronized TreeSet holding all chunk metadata belonging to unloaded
	 * chunks. A comparator based on when the chunk was unloaded is used to
	 * guarantee an iteration order ascending based on unloading time, which makes
	 * cleanup trivial
	 */
	private final Set<ChunkCoord> unloadingQueue;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private Thread chunkLoadingConsumer;
	private Queue<ChunkCoord> chunkLoadingQueue;
	private World world;

	public WorldChunkMetaManager(World world, short worldID) {
		this.worldID = worldID;
		this.world = world;
		this.metas = new HashMap<>();
		this.unloadingQueue = Collections.synchronizedSet(new TreeSet<>((a, b) -> {
			return Math.toIntExact(a.getLastMCUnloadingTime() - b.getLastMCUnloadingTime());
		}));
		registerUnloadRunnable();
		startChunkLoadingConsumer();
	}

	ChunkMeta<?> computeIfAbsent(int pluginID, int x, int z, Supplier<ChunkMeta<?>> computer) {
		ChunkCoord coord = getChunkCoord(x, z, true, false);
		ChunkMeta<?> existing = coord.getMeta(pluginID);
		if (existing != null) {
			return existing;
		}
		existing = computer.get();
		existing.setChunkCoord(coord);
		existing.setPluginID(pluginID);
		coord.addChunkMeta(existing);
		return existing;
	}

	/**
	 * Saves all entries to the database
	 */
	void flushAll() {
		synchronized (metas) {
			for (ChunkCoord coord : metas.keySet()) {
				synchronized (coord) {
					coord.fullyPersist();
				}
			}
		}
	}

	/**
	 * Retrieves or generates a new ChunkCoord instance. ChunkCoord are each
	 * singletons for their location, which is enforced through this method
	 * 
	 * @param x   X-coordinate of the chunk
	 * @param z   Z-coordinate of the chunk
	 * @param gen Should a new ChunkCoord be generated if none exists at the given
	 *            location
	 * @param gen Should data for the ChunkCoord be loaded from the database if a
	 *            new one was created
	 * @return Found/Generated ChunkCoord or null if none existed and none was
	 *         supposed to be generated
	 */
	private ChunkCoord getChunkCoord(int x, int z, boolean gen, boolean populate) {
		ChunkCoord coord = new ChunkCoord(x, z, worldID, world);
		synchronized (metas) {
			ChunkCoord value = metas.get(coord);
			if (value != null) {
				return value;
			}
			if (!gen) {
				return null;
			}
			metas.put(coord, coord);
			if (populate) {
				// up until here we are still sync from the ChunkLoadEvent, so we need to
				// offload the actual db load to another thread
				synchronized (chunkLoadingQueue) {
					chunkLoadingQueue.add(coord);
					chunkLoadingQueue.notifyAll();
				}
			}
			return coord;
		}
	}

	/**
	 * Retrieves the chunk meta for a specific chunk for a specific plugin.
	 * 
	 * DO NOT USE THIS FOR UNLOADED CHUNKS, THINGS WILL BREAK HORRIBLY
	 * 
	 * @param pluginID Internal id of the plugin
	 * @param x        X-coordinate of the chunk
	 * @param z        Z-coordinate of the chunk
	 * @return ChunkMeta for the given parameter, possibly null if none existed
	 */
	ChunkMeta<?> getChunkMeta(int pluginID, int x, int z) {
		ChunkCoord coord = getChunkCoord(x, z, false, false);
		if (coord == null) {
			return null;
		}
		return coord.getMeta(pluginID);
	}

	/**
	 * Inserts new chunk metadata, overwriting any existing one for the same plugin
	 * and the same chunk
	 * 
	 * @param x    X-coordinate of the chunk
	 * @param z    Z-coordinate of the chunk
	 * @param meta Metadata to insert
	 */
	void insertChunkMeta(int x, int z, ChunkMeta<?> meta) {
		ChunkCoord coord = getChunkCoord(x, z, true, false);
		meta.setChunkCoord(coord);
		coord.addChunkMeta(meta);
	}

	/**
	 * Called when the underlying minecraft chunk is loaded. Loads the chunk
	 * metadata from the database if its not already available in the cache
	 * 
	 * @param x X-coordinate of the chunk
	 * @param z Z-coordinate of the chunk
	 */
	void loadChunk(int x, int z) {
		ChunkCoord chunkCoord = getChunkCoord(x, z, true, true);
		if (chunkCoord.getLastMCUnloadingTime() != -1) {
			unloadingQueue.remove(chunkCoord);
		}
		chunkCoord.minecraftChunkLoaded();
	}

	private void registerUnloadRunnable() {
		scheduler.scheduleWithFixedDelay(() -> {
			if (unloadingQueue.isEmpty()) {
				return;
			}
			long currentTime = System.currentTimeMillis();
			synchronized (unloadingQueue) {
				Iterator<ChunkCoord> iter = unloadingQueue.iterator();
				while (iter.hasNext()) {
					ChunkCoord coord = iter.next();
					// Is time up?
					if (currentTime - coord.getLastMCUnloadingTime() > UNLOAD_DELAY
							// make sure chunk hasnt loaded again since
							&& coord.getLastMCUnloadingTime() > coord.getLastMCLoadingTime()) {
						synchronized (metas) {
							synchronized (coord) {
								coord.fullyPersist();
								metas.remove(coord);
								iter.remove();
								// coord is up for garbage collection at this point and all of its data has been
								// written to the db
							}
						}
					}
				}
			}

		}, UNLOAD_CHECK_INTERVAL, UNLOAD_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
	}

	private void startChunkLoadingConsumer() {
		this.chunkLoadingQueue = new LinkedBlockingQueue<>();
		chunkLoadingConsumer = new Thread(() -> {
			while (true) {
				ChunkCoord coord;
				synchronized (chunkLoadingQueue) {
					while (chunkLoadingQueue.isEmpty()) {
						try {
							chunkLoadingQueue.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					coord = chunkLoadingQueue.poll();
					if (coord == null) {
						continue;
					}
				}
				coord.loadAll();
			}
		});
		chunkLoadingConsumer.start();
	}

	/**
	 * Called when the underlying minecraft chunk is unloaded. Does not actually
	 * unload our data, but instead stages it to be unloaded if the chunk stays
	 * unloaded for a certain period of time
	 * 
	 * @param x X-coordinate of the chunk
	 * @param z Z-coordinate of the chunk
	 */
	void unloadChunk(int x, int z) {
		ChunkCoord chunkCoord = getChunkCoord(x, z, false, false);
		// chunkCoord can never be null here, otherwise our data structure would be
		// broken, in which case we'd want to know
		chunkCoord.minecraftChunkUnloaded();
		unloadingQueue.add(chunkCoord);
	}
}
