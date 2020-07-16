package com.github.igotyou.FactoryMod;

import com.github.igotyou.FactoryMod.listeners.CitadelListener;
import com.github.igotyou.FactoryMod.listeners.CompactItemListener;
import com.github.igotyou.FactoryMod.listeners.FactoryModListener;
import com.github.igotyou.FactoryMod.utility.FactoryModPermissionManager;
import com.github.igotyou.FactoryMod.commands.CheatOutput;
import com.github.igotyou.FactoryMod.commands.Create;
import com.github.igotyou.FactoryMod.commands.Menu;
import com.github.igotyou.FactoryMod.commands.RunAmountSetterCommand;

import vg.civcraft.mc.civmodcore.ACivMod;

public class FactoryMod extends ACivMod {
	private FactoryModManager manager;
	private static FactoryMod plugin;
	private FactoryModPermissionManager permissionManager;

	@Override
	public void onEnable() {
		super.onEnable();
		plugin = this;
		ConfigParser cp = new ConfigParser(this);
		manager = cp.parse();
		manager.loadFactories();
		if (manager.isCitadelEnabled()) {
			permissionManager = new FactoryModPermissionManager();
		}
		registerListeners();

		// Still not sure why loadAll() isn't working, but whatever
		this.getStandaloneCommandHandler().registerCommand(new RunAmountSetterCommand());
		this.getStandaloneCommandHandler().registerCommand(new CheatOutput());
		this.getStandaloneCommandHandler().registerCommand(new Menu());
		this.getStandaloneCommandHandler().registerCommand(new Create());
		
		info("Successfully enabled");
	}

	@Override
	public void onDisable() {
		manager.shutDown();
		plugin.info("Shutting down");
	}

	public FactoryModManager getManager() {
		return manager;
	}

	public static FactoryMod getInstance() {
		return plugin;
	}
	
	public FactoryModPermissionManager getPermissionManager() {
		return permissionManager;
	}

	private void registerListeners() {
		plugin.getServer().getPluginManager()
				.registerEvents(new FactoryModListener(manager), plugin);
		plugin.getServer()
				.getPluginManager()
				.registerEvents(
						new CompactItemListener(), plugin);
		if (manager.isCitadelEnabled()) {
			plugin.getServer().getPluginManager().registerEvents(new CitadelListener(), plugin);
		}
	}
}
