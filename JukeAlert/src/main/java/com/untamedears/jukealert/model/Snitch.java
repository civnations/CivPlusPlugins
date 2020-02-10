package com.untamedears.jukealert.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import com.untamedears.jukealert.model.appender.AbstractSnitchAppender;
import com.untamedears.jukealert.model.field.FieldManager;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class Snitch extends TableBasedDataObject {

	private int snitchId;
	private String name;
	private int groupID;
	private Map<Class<? extends AbstractSnitchAppender>, AbstractSnitchAppender> appenders;
	private FieldManager fieldManager;
	private SnitchFactory type;
	private boolean active;

	/**
	 * 
	 * Constructor used when loading snitches from the database
	 * 
	 * @param loc          Location of the snitch
	 * @param isNew        Whether the snitch is new, should always be false when
	 *                     calling from outside this class
	 * @param groupID      ID of the group the snitch is reinforced on
	 * @param fieldManager FieldManager to control the range of the snitch
	 * @param type         FieldManager to control the range of the snitch
	 * @param name         Name of the snitch
	 */
	public Snitch(int snitchID, Location loc, boolean isNew, int groupID,
			Function<Snitch, FieldManager> fieldManagerFunc, SnitchFactory type, String name) {
		super(loc, isNew);
		this.snitchId = snitchID;
		this.groupID = groupID;
		this.name = name;
		this.fieldManager = fieldManagerFunc.apply(this);
		this.type = type;
		this.appenders = new HashMap<>();
		this.active = true;
	}

	/**
	 * Adds a new appender to this snitch
	 * 
	 * @param appender Appender to add
	 */
	public void addAppender(AbstractSnitchAppender appender) {
		appenders.put(appender.getClass(), appender);
	}

	/**
	 * Checks whether this instance has an appender of the given type
	 * 
	 * @param appenderClass Class of the appender to check fpr
	 * @return True if this instance has such an appender, false otherwise
	 */
	public boolean hasAppender(Class<? extends AbstractSnitchAppender> appenderClass) {
		return appenders.containsKey(appenderClass);
	}

	/**
	 * Gets this instances appender of the given type
	 * 
	 * @param appenderClass Type of the appender to retrieve
	 * @return Appender of this instance of null if no such appender is held
	 */
	public AbstractSnitchAppender getAppender(Class<? extends AbstractSnitchAppender> appenderClass) {
		return appenders.get(appenderClass);
	}

	/**
	 * @return Type/Config of this snitch
	 */
	public SnitchFactory getType() {
		return type;
	}

	/**
	 * Checks if the player with the given UUID has the given permission for this
	 * snitch
	 * 
	 * @param uuid       UUID of the player
	 * @param permission Permission to check for
	 * @return True if the player has the permission and the snitch/group is valid,
	 *         false otherwise
	 */
	public boolean hasPermission(UUID uuid, PermissionType permission) {
		return NameAPI.getGroupManager().hasAccess(getGroup(), uuid, permission);
	}

	/**
	 * Checks if the player with the given UUID has the given permission for this
	 * snitch
	 * 
	 * @param uuid       Player to check for
	 * @param permission Permission to check for
	 * @return True if the player has the permission and the snitch/group is valid,
	 *         false otherwise
	 */
	public boolean hasPermission(Player player, PermissionType permission) {
		return hasPermission(player.getUniqueId(), permission);
	}

	/**
	 * @return FieldManager which handles the exact area covered by the snitch
	 */
	public FieldManager getFieldManager() {
		return fieldManager;
	}

	/**
	 * @return Group the snitch belongs to
	 */
	public Group getGroup() {
		return GroupManager.getGroup(groupID);
	}

	/**
	 * @return Database internal id of the snitch. -1 if not set yet
	 */
	public int getId() {
		return snitchId;
	}

	/**
	 * @return Custom name of the snitch, empty String by default
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the internal database id of the snitch, should only be used after first
	 * inserting the snitch into the database
	 * 
	 * @param newId Snitch id
	 */
	public void setId(int newId) {
		this.snitchId = newId;
	}

	/**
	 * Changes the custom player given name of the snitch
	 * 
	 * @param name New name
	 */
	public void setName(String name) {
		this.name = name;
		setDirty();
	}

	/**
	 * Takes an action and processes it through all appenders tied to this snitch
	 * 
	 * @param action Action to pass through
	 */
	public void processAction(SnitchAction action) {
		for (AbstractSnitchAppender appender : appenders.values()) {
			if (!active && !appender.runWhenSnitchInactive()) {
				continue;
			}

			appender.acceptAction(action);
		}
	}

	/**
	 * Sets the active state of the snitch. Certain appenders may choose not to run
	 * if the snitch is inactive
	 * 
	 * @param active Whether the snitch should be active or not
	 */
	public void setActiveStatus(boolean active) {
		this.active = active;
	}

	/**
	 * Forces all appenders of this snitch to persist their current state
	 */
	public void persistAppenders() {
		applyToAppenders(AbstractSnitchAppender::persist);
	}

	/**
	 * Applies the given consumer to all appenders of this snitch instance
	 * 
	 * @param con Function to apply
	 */
	public void applyToAppenders(Consumer<AbstractSnitchAppender> con) {
		appenders.values().forEach(con);
	}
}
