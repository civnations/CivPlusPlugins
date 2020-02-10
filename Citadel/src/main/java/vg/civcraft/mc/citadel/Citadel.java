package vg.civcraft.mc.citadel;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import vg.civcraft.mc.citadel.listener.BlockListener;
import vg.civcraft.mc.citadel.listener.EntityListener;
import vg.civcraft.mc.citadel.listener.InformationModeListener;
import vg.civcraft.mc.citadel.listener.InventoryListener;
import vg.civcraft.mc.citadel.listener.RedstoneListener;
import vg.civcraft.mc.citadel.model.AcidManager;
import vg.civcraft.mc.citadel.model.CitadelChunkData;
import vg.civcraft.mc.citadel.model.CitadelSettingManager;
import vg.civcraft.mc.citadel.model.CitadelStorage;
import vg.civcraft.mc.citadel.model.HologramManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.playerstate.PlayerStateManager;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementTypeManager;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.BlockBasedChunkMetaView;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;

public class Citadel extends ACivMod {

	private static Citadel instance;

	public static Citadel getInstance() {
		return instance;
	}

	private Logger logger;
	private ReinforcementManager reinManager;
	private CitadelConfigManager config;
	private AcidManager acidManager;
	private ReinforcementTypeManager typeManager;
	private HologramManager holoManager;
	private CitadelSettingManager settingManager;

	private PlayerStateManager stateManager;

	/**
	 * @return Acid block manager
	 */
	public AcidManager getAcidManager() {
		return acidManager;
	}

	public CitadelConfigManager getConfigManager() {
		return config;
	}

	public ReinforcementManager getReinforcementManager() {
		return reinManager;
	}

	public ReinforcementTypeManager getReinforcementTypeManager() {
		return typeManager;
	}

	public CitadelSettingManager getSettingManager() {
		return settingManager;
	}

	public PlayerStateManager getStateManager() {
		return stateManager;
	}

	public HologramManager getHologramManager() {
		return holoManager;
	}

	@Override
	public void onDisable() {
		reinManager.shutDown();
		HandlerList.unregisterAll(this);
		Bukkit.getScheduler().cancelTasks(this);
	}

	public void reload() {
		onDisable();
		onEnable();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;
		logger = getLogger();
		if (!Bukkit.getPluginManager().isPluginEnabled("NameLayer")) {
			logger.info("Citadel is shutting down because it could not find NameLayer");
			Bukkit.shutdown();
			return;
		}
		config = new CitadelConfigManager(this);
		if (!config.parse()) {
			logger.severe("Errors in config file, shutting down");
			Bukkit.shutdown();
			return;
		}
		typeManager = new ReinforcementTypeManager();
		config.getReinforcementTypes().forEach(t -> typeManager.register(t));
		CitadelStorage storage = new CitadelStorage(this.logger, config.getDatabase());
		if (!storage.updateDatabase()) {
			logger.severe("Errors setting up database, shutting down");
			Bukkit.shutdown();
			return;
		}
		BlockBasedChunkMetaView<CitadelChunkData, TableBasedDataObject, TableStorageEngine<Reinforcement>> chunkMetaData = 
				ChunkMetaAPI.registerBlockBasedPlugin(this, () -> {return new CitadelChunkData(false, storage);});
		if (chunkMetaData == null) {
			logger.severe("Errors setting up chunk metadata API, shutting down");
			Bukkit.shutdown();
			return;
		}
		reinManager = new ReinforcementManager(chunkMetaData);
		stateManager = new PlayerStateManager();
		acidManager = new AcidManager(config.getAcidMaterials());
		settingManager = new CitadelSettingManager();
		if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			holoManager = new HologramManager(settingManager);
		}
		CitadelPermissionHandler.setup();
		registerListeners();
	}

	/**
	 * Registers the listeners for Citadel.
	 */
	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new BlockListener(this), this);
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		getServer().getPluginManager().registerEvents(new InformationModeListener(this), this);
		getServer().getPluginManager().registerEvents(new RedstoneListener(config.getMaxRedstoneDistance()), this);
	}
}
