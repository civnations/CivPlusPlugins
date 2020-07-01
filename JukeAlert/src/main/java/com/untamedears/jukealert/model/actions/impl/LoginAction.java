package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import com.untamedears.jukealert.events.PlayerLoginSnitchEvent;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;

import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class LoginAction extends LoggablePlayerAction {

	public static final String ID = "LOGIN";

	public LoginAction(long time, Snitch snitch, UUID player) {
		super(time, snitch, player);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = getSkullFor(getPlayer());
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public void accept(Snitch s) {
		Bukkit.getPluginManager().callEvent(new PlayerLoginSnitchEvent(snitch, Bukkit.getPlayer(player)));
	}

	@Override
	protected String getChatRepresentationIdentifier() {
		return ChatColor.BOLD + "Login";
	}
}
