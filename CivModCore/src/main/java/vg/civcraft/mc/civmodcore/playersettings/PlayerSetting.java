package vg.civcraft.mc.civmodcore.playersettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Preconditions;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;

/**
 * Contains a value for every players for one setting
 */
public abstract class PlayerSetting<T> {

	private Map<UUID, T> values;
	private T defaultValue;
	private ItemStack visualization;
	private String description;
	private String identifier;
	private String niceName;
	private JavaPlugin owningPlugin;
	private List<SettingChangeListener<T>> listeners;
	private boolean canBeChangedByPlayer;

	public PlayerSetting(JavaPlugin owningPlugin, T defaultValue, String niceName, String identifier, ItemStack gui,
			String description, boolean canBeChangedByPlayer) {
		Preconditions.checkNotNull(gui, "GUI ItemStack can not be null.");

		values = new TreeMap<>();
		this.defaultValue = defaultValue;
		this.owningPlugin = owningPlugin;
		this.niceName = niceName;
		this.identifier = identifier;
		this.visualization = gui;
		this.description = description;
		this.canBeChangedByPlayer = canBeChangedByPlayer;
	}

	protected void applyInfoToItemStack(ItemStack item, UUID player) {
		ItemAPI.setDisplayName(item, niceName);
		ItemAPI.addLore(item, ChatColor.LIGHT_PURPLE + "Value: " + ChatColor.RESET + toText(getValue(player)));
		if (description != null) {
			ItemAPI.addLore(item, description);
		}
	}

	/**
	 * Recreates an instance from a serialized
	 * 
	 * @param serial
	 * @return
	 */
	public abstract T deserialize(String serial);

	Map<String, String> dumpAllSerialized() {
		Map<String, String> result = new HashMap<>();
		for (Entry<UUID, T> entry : values.entrySet()) {
			result.put(entry.getKey().toString(), serialize(entry.getValue()));
		}
		return result;
	}

	/**
	 * @return Textual description shown in the GUI for this setting
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the item stack used in the configuration GUI. Feel encouraged to
	 * overwrite this in implementations
	 * 
	 * @param player UUID of the player opening the GUI
	 * @return ItemStack to show for this setting
	 */
	public ItemStack getGuiRepresentation(UUID player) {
		ItemStack copy;
		if (visualization == null) {
			copy = new ItemStack(MaterialAPI.getMaterialHash(getValue(player)));
		}
		else {
			copy = visualization.clone();
		}
		applyInfoToItemStack(copy, player);
		return copy;
	}

	/**
	 * @return Human readable name to use in GUIs etc.
	 */
	public String getNiceName() {
		return niceName;
	}

	/**
	 * @return Plugin which created this setting
	 */
	public JavaPlugin getOwningPlugin() {
		return owningPlugin;
	}

	/**
	 * Gets the stored value for the given player or the default value if the player
	 * has no own value
	 * 
	 * @param player UUID of the player to get value for
	 * @return Value for the player or default value
	 */
	public T getValue(UUID player) {
		T value = values.get(player);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * Gets the stored value for the given player or the default value if the player
	 * has no own value
	 * 
	 * @param player Player to get value for
	 * @return Value for the player or default value
	 */
	public T getValue(Player player) {
		return getValue(player.getUniqueId());
	}
	
	/**
	 * @return Can the owning player freely edit this value
	 */
	public boolean canBeChangedByPlayer() {
		return canBeChangedByPlayer;
	}

	/**
	 * Called when this setting is clicked in a menu to adjust its value
	 * 
	 */
	public void handleMenuClick(Player player, MenuSection menu) {
		new MenuDialog(player, this, menu, "Invalid input");
	}
	
	public void setValueFromString(UUID player, String inputValue) {
		T value = deserialize(inputValue);
		setValue(player, value);
	}
	
	public String getSerializedValueFor(UUID player) {
		return serialize(getValue(player));
	}
	
	/**
	 * Input validation to confirm player entered values are not malformed
	 * 
	 * @param input Input string to test
	 * @return True if the input can be parsed as valid value, false otherwise
	 */
	public abstract boolean isValidValue(String input);
	
	void load(String player, String serial) {
		UUID uuid = UUID.fromString(player);
		T value = deserialize(serial);
		setValue(uuid, value);
	}

	public abstract String serialize(T value);

	/**
	 * Sets the given value for the given player. Null values are only allowed if
	 * the (de-)serialization implementation can properly handle it, which is not
	 * the case for any of the implementations provided here
	 * 
	 * @param player UUID of the player to set value for
	 * @param value  New value
	 */
	public void setValue(UUID player, T value) {
		if (listeners != null) {
			T oldValue = getValue(player);
			for(SettingChangeListener<T> listener: listeners) {
				listener.handle(player, this, oldValue, value);
			}
		}
		values.put(player, value);
	}

	/**
	 * Sets the given value for the given player. Null values are only allowed if
	 * the (de-)serialization implementation can properly handle it, which is not
	 * the case for any of the implementations provided here
	 * 
	 * @param player Player to set value for
	 * @param value  New value
	 */
	public void setValue(Player player, T value) {
		setValue(player.getUniqueId(), value);
	}

	/**
	 * @return Unique identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Registers a listener which will be triggered if the value is updated
	 * 
	 * @param listener Listener to register
	 */
	public void registerListener(SettingChangeListener<T> listener) {
		if (this.listeners == null) {
			this.listeners = new ArrayList<>();
		}
		this.listeners.add(listener);
	}
	
	/**
	 * Unregisters a listener
	 * 
	 * @param listener Listener to unregister
	 */
	public void unregisterListener(SettingChangeListener<T> listener) {
		if (this.listeners != null) {
			this.listeners.remove(listener);
		}
	}

	/**
	 * Creates a textual representation of a value to use in GUIs
	 * 
	 * @param value Value to get text for
	 * @return GUI text
	 */
	public abstract String toText(T value);
}
