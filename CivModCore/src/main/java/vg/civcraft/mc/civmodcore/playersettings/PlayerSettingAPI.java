package vg.civcraft.mc.civmodcore.playersettings;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuOption;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Allows creating settings, which will automatically be available in players
 * configuration GUI
 *
 */
public final class PlayerSettingAPI {
	
	private PlayerSettingAPI() {}

	private static final String FILE_NAME = "civ-player-settings.yml";

	private static Map<String, PlayerSetting<?>> settingsByIdentifier = new HashMap<>();
	private static Map<String, List<PlayerSetting<?>>> settingsByPlugin = new HashMap<>();

	private static MenuSection mainMenu = new MenuSection("Config", "", null);

	/**
	 * @return GUI main menu
	 */
	public static MenuSection getMainMenu() {
		return mainMenu;
	}

	/**
	 * Gets a setting by its identifier
	 * 
	 * @param identifier Identifier to get setting for
	 * @return Setting with the given identifier or null if no such setting exists
	 */
	public static PlayerSetting<?> getSetting(String identifier) {
		return settingsByIdentifier.get(identifier);
	}

	private static void loadValues(PlayerSetting<?> setting) {
		File folder = setting.getOwningPlugin().getDataFolder();
		if (!folder.isDirectory()) {
			return;
		}
		File file = new File(folder, FILE_NAME);
		if (!file.isFile()) {
			return;
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		ConfigurationSection section = config.getConfigurationSection(setting.getIdentifier());
		if (section == null) {
			return;
		}
		for (String key : section.getKeys(false)) {
			setting.load(key, section.getString(key));
		}
	}

	/**
	 * Settings must be registered on every startup to be available. Identifiers
	 * must be unique globally.
	 * 
	 * If a setting had values assigned but is not registered on startup its old
	 * values will be left alone.
	 * 
	 * @param setting Setting to register
	 * @param menu Menu in which this value will appear
	 */
	public static void registerSetting(PlayerSetting<?> setting, MenuSection menu) {
		loadValues(setting);
		settingsByIdentifier.put(setting.getIdentifier(), setting);
		List<PlayerSetting<?>> specificList = settingsByPlugin.computeIfAbsent(setting.getOwningPlugin().getName(), k -> new ArrayList<>());
		if (specificList.contains(setting)) {
			throw new IllegalArgumentException("You can not register a setting twice");
		}
		specificList.add(setting);
		if (setting.canBeChangedByPlayer()) {
			menu.addItem(new MenuOption(menu, setting));
		}
	}

	/**
	 * Saves all values to their save files
	 */
	public static void saveAll() {
		for (Entry<String, List<PlayerSetting<?>>> pluginEntry : settingsByPlugin.entrySet()) {
			if (pluginEntry.getValue().isEmpty()) {
				continue;
			}
			File folder = pluginEntry.getValue().get(0).getOwningPlugin().getDataFolder();
			if (!folder.isDirectory()) {
				folder.mkdirs();
			}
			File file = new File(folder, FILE_NAME);
			YamlConfiguration config;
			if (file.isFile()) {
				config = YamlConfiguration.loadConfiguration(file);
			}
			else {
				config = new YamlConfiguration();
			}
			for (PlayerSetting<?> setting : pluginEntry.getValue()) {
				ConfigurationSection section;
				if (config.isConfigurationSection(setting.getIdentifier())) {
					section = config.getConfigurationSection(setting.getIdentifier());
				} else {
					section = config.createSection(setting.getIdentifier());
				}
				for (Entry<String, String> entry : setting.dumpAllSerialized().entrySet()) {
					section.set(entry.getKey(), entry.getValue());
				}
			}
			try {
				config.save(file);
			} catch (IOException e) {
				CivModCorePlugin.getInstance().severe("Failed to save settings", e);
			}
		}
	}

}
