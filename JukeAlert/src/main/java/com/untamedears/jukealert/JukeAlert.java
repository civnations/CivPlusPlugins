package com.untamedears.jukealert;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import com.untamedears.jukealert.database.JukeAlertDAO;
import com.untamedears.jukealert.listener.LoggableActionListener;
import com.untamedears.jukealert.listener.SnitchLifeCycleListener;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchChunkData;
import com.untamedears.jukealert.model.SnitchQTEntry;
import com.untamedears.jukealert.model.SnitchTypeManager;
import com.untamedears.jukealert.model.actions.LoggedActionFactory;
import com.untamedears.jukealert.util.JASettingsManager;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.locations.SparseQuadTree;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.BlockBasedChunkMetaView;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;

public class JukeAlert extends ACivMod {

	private static JukeAlert instance;

	public static JukeAlert getInstance() {
		return instance;
	}
	
	private JukeAlertDAO dao;
	private JAConfigManager configManager;
	private SnitchTypeManager snitchConfigManager;
	private SnitchManager snitchManager;
	private SparseQuadTree<SnitchQTEntry> quadTree;
	private LoggedActionFactory loggedActionFactory;
	private JASettingsManager settingsManager;
	private SnitchCullManager cullManager;

	public JAConfigManager getConfigManager() {
		return configManager;
	}
	
	public JASettingsManager getSettingsManager() {
		return settingsManager;
	}
	
	public LoggedActionFactory getLoggedActionFactory() {
		return loggedActionFactory;
	}
	
	public SnitchTypeManager getSnitchConfigManager() {
		return snitchConfigManager;
	}

	public JukeAlertDAO getDAO() {
		return dao;
	}
	
	public SnitchManager getSnitchManager() {
		return snitchManager;
	}
	
	public SparseQuadTree<SnitchQTEntry> getQuadTree() {
		return quadTree;
	}
	
	public SnitchCullManager getSnitchCullManager() {
		return cullManager;
	}

	@Override
	public void onDisable() {
		
	}

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
		quadTree = new SparseQuadTree<>(1);
		snitchConfigManager = new SnitchTypeManager();
		cullManager = new SnitchCullManager();
		configManager = new JAConfigManager(this, snitchConfigManager);
		if (!configManager.parse()) {
			Bukkit.shutdown();
			return;
		}
		dao = new JukeAlertDAO(getLogger(), configManager.getDatabase());
		if (!dao.updateDatabase()) {
			getLogger().severe("Errors setting up database, shutting down");
			Bukkit.shutdown();
			return;
		}
		BlockBasedChunkMetaView<SnitchChunkData, TableBasedDataObject, TableStorageEngine<Snitch>> chunkMetaData = 
				ChunkMetaAPI.registerBlockBasedPlugin(this, () -> {return new SnitchChunkData(false, dao);});
		if (chunkMetaData == null) {
			getLogger().severe("Errors setting up chunk metadata API, shutting down");
			Bukkit.shutdown();
			return;
		}
		snitchManager = new SnitchManager(chunkMetaData, quadTree);
		loggedActionFactory = new LoggedActionFactory();
		settingsManager = new JASettingsManager();
		registerJukeAlertEvents();
		JukeAlertPermissionHandler.setup();
	}

	private void registerJukeAlertEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new LoggableActionListener(snitchManager), this);
		pm.registerEvents(new SnitchLifeCycleListener(snitchManager, snitchConfigManager, getLogger()), this);
	}
}
